from sqlalchemy.orm import relationship, registry, declarative_base, Session
from sqlalchemy import Column, String, Integer, create_engine, text, MetaData, Table, select, delete

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from functools import wraps
import random

from datetime import datetime, timezone, timedelta
import time

engine = create_engine("sqlite:///db2.sqlite", echo=False)
session = Session(engine)

metadata = MetaData()

users_table = Table("Users", metadata, autoload_with=engine)
auth_table = Table("Auth", metadata, autoload_with=engine)

Base = declarative_base()

class User(Base):
    __tablename__ = "Users"

    id = Column(Integer, primary_key=True)
    username = Column(String(10))
    password_hash = Column(String(256))
    first_name = Column(String(20))
    last_name = Column(String(20))
    status = Column(Integer)

    def __repr__(self):
        return f"User(id={self.username}, name={self.first_name} {self.last_name}, status={self.status})"

class Class(Base):
    __tablename__ = "Classes"

    id = Column(Integer, primary_key=True)
    student = Column(String(10))
    module = Column(String(10))

    def __repr__(self):
        return f"Class(student={self.student}, module={self.module})"

class Module(Base):
    __tablename__ = "Modules"

    id = Column(Integer, primary_key=True)
    module_id = Column(String(10))
    module_name = Column(String(50))
    module_lead = Column(String(10))

    def __repr__(self):
        return f"Module(module_id={self.module_id}, module_name={self.module_name}, module_lead={self.module_lead})"

class Schedule(Base):
    __tablename__ = "Schedule"

    id = Column(Integer, primary_key=True)
    module_id = Column(String(10))
    date = Column(Integer)
    description = Column(String(50))

    def __repr__(self):
        return f"Schedule(module_id={self.module_id}, date={self.date}, description={self.description})"

class Auth(Base):
    __tablename__ = "Auth"

    id = Column(Integer, primary_key=True)
    auth_string = Column(String(256))
    user = Column(String(10))

    def __repr__(self):
        return f"Auth(user={self.user}, auth_string={self.auth_string})"

class Todo(Base):
    __tablename__ = "Todo"

    id = Column(Integer, primary_key=True)
    schedule_id = Column(Integer)
    student = Column(String(10))
    status = Column(Integer)

app = FastAPI()

class LoginCredentials(BaseModel):
    username: str
    password_hash: str

class SignupCredentials(BaseModel):
    username: str
    password_hash: str
    first_name: str
    last_name: str
    status: int

class AuthString(BaseModel):
    auth_string: str

class ModuleData(BaseModel):
    auth_string: str
    module_id: str

class NewModuleData(BaseModel):
    auth_string: str
    module_id: str
    module_name: str

class NewScheduleData(BaseModel):
    auth_string: str
    module_id: str
    date: str
    description: str

class ScheduleData(BaseModel):
    auth_string: str
    id: int

def generate_auth_string(user: User):
    auth_string = "".join([random.choice("abcdefghijklmnopqrstuvwxyz1234567899") for _ in range(256)])
    auth = Auth(auth_string=auth_string, user=user.username)
    old_auth = session.execute(select(Auth).where(Auth.user == user.username)).first()
    if old_auth is not None:
        session.delete(old_auth[0])
    session.add(auth)
    session.commit()
        
    return auth_string

def result_to_dict(result):
    return {column.name: getattr(result, column.name) for column in result.__table__.columns}

@app.post("/login/")
async def login(login_credentials: LoginCredentials):
    username = login_credentials.username
    password_hash = login_credentials.password_hash
    user = session.execute(select(User).where(User.username == username)).first()

    if user is not None and user[0].password_hash == password_hash:
        auth_string = generate_auth_string(user[0])
        return {"auth_string" : auth_string, "state" : user[0].status}
    raise HTTPException(status_code=418, detail="Invalid login credentials.")

@app.post("/logout/")
async def logout(auth_string: AuthString):
    auth = session.execute(select(Auth).where(Auth.auth_string == auth_string.auth_string)).first()

    if auth is not None:
        session.delete(auth[0])
        session.commit()
        return
    raise HTTPException(status_code=418, detail="Invalid login credentials.")

@app.post('/signup/')
async def signup(signup_credentials: SignupCredentials):
    username = signup_credentials.username
    password_hash = signup_credentials.password_hash
    first_name = signup_credentials.first_name
    last_name = signup_credentials.last_name
    status = signup_credentials.status

    user = session.execute(select(User).where(User.username == username)).first()
    if user is None:
        new_user = User(username=username, password_hash=password_hash, first_name=first_name, last_name=last_name, status=status)
        session.add(new_user)
        session.commit()
        auth_string = generate_auth_string(new_user)
        return {"auth_string" : auth_string, "state" : new_user.status}
    raise HTTPException(status_code=418, detail="User already exists.")

@app.post('/student/schedule/')
async def student_schedule(auth_string: AuthString):
    user = session.execute(select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == auth_string.auth_string)
    ).first()

    if user is not None:
        if user[0].status == 0:
            schedule = session.execute(
                select(Schedule)
                .join(Class, Schedule.module_id == Class.module)
                .join(User, Class.student == User.username)
                .where(User.username == user[0].username)
            ).scalars().all()

            # TODO: Fix table relationships and remove this nightmare
            ret_val = []
            tz = timezone(timedelta(hours=8))

            for s in schedule:
                module = session.execute(
                    select(Module)
                    .where(Module.module_id == s.module_id)
                ).first()

                completed = session.execute(
                    select(Todo)
                    .where(Todo.schedule_id == s.id)
                    .where(Todo.student == user[0].username)
                ).scalars().all()
                
                if len(completed) == 0:
                    completed = "Not Done"
                else:
                    completed = ("Not Done", "Done")[completed[0].status]

                ret_val.append(
                    {
                        "id" : s.id,
                        "module_name" : module[0].module_name,
                        "date" : datetime.fromtimestamp(s.date, tz).strftime("%d/%m/%Y %H:%M"),
                        "description" : s.description,
                        "done" : completed
                    }
                )
            return sorted(ret_val, key=lambda x:x["date"])
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/student/schedule/toggle/')
async def student_schedule_toggle(schedule_data: ScheduleData):
    print(schedule_data)
    user = session.execute(select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == schedule_data.auth_string)
    ).first()

    if user is not None:
        if user[0].status == 0:
            todo = session.execute(
                select(Todo)
                .where(Todo.student == user[0].username)
                .where(Todo.schedule_id == schedule_data.id)
            ).first()

            if todo is None:
                todo = [Todo(schedule_id=schedule_data.id, student=user[0].username, status=0)]
                session.add(todo[0])
            
            todo[0].status = not todo[0].status

            session.commit()

            return
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/schedule/')
# TODO: Add tally for each deadline
async def faculty_schedule(auth_string: AuthString):
    user = session.execute(select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == auth_string.auth_string)
    ).first()

    if user is not None:
        if user[0].status == 1:
            schedule = session.execute(
                select(Schedule)
                .join(Module, Schedule.module_id == Module.module_id)
                .join(User, Module.module_lead == User.username)
                .where(User.username == user[0].username)
            ).scalars().all()

            # TODO: Fix table relationships and remove this nightmare
            ret_val = []
            tz = timezone(timedelta(hours=8))

            for s in schedule:
                module = session.execute(
                    select(Module)
                    .where(Module.module_id == s.module_id)
                ).first()

                completed = session.execute(
                    select(Todo)
                    .where(Todo.schedule_id == s.id)
                    .where(Todo.status == 1)
                ).scalars().all()

                total_students = session.execute(
                    select(Class)
                    .where(Class.module == s.module_id)
                ).scalars().all()

                ret_val.append(
                    {
                        "id" : s.id,
                        "module_name" : module[0].module_name,
                        "date" : datetime.fromtimestamp(s.date, tz).strftime("%d/%m/%Y %H:%M"),
                        "description" : s.description,
                        "tally" : f"{len(completed)} / {len(total_students)}"
                    }
                )

            return sorted(ret_val, key=lambda x:x["date"])
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/schedule/add/')
async def faculty_schedule_add(schedule_data: NewScheduleData):
    user = session.execute(select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == schedule_data.auth_string)
    ).first()

    if user is not None:
        if user[0].status == 1:
            module_exists = session.execute(
                select(Module)
                .where(Module.module_id == schedule_data.module_id)
            )

            if module_exists is None:
                raise HTTPException(status_code=418, detail="Module does not exist.")

            try:
                date = datetime.strptime(schedule_data.date, "%d/%m/%Y %H:%M")
            except:
                raise HTTPException(status_code=418, detail="Invalid date string provided.")

            if datetime.today() > date:
                raise HTTPException(status_code=418, detail="Date provided has already passed.")

            date = time.mktime(date.timetuple())

            schedule = Schedule(module_id=schedule_data.module_id, date=date, description=schedule_data.description)
            session.add(schedule)
            session.commit()

            return 
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/schedule/delete/')
async def faculty_schedule_delete(schedule_data: ScheduleData):
    user = session.execute(select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == schedule_data.auth_string)
    ).first()

    if user is not None:
        if user[0].status == 1:
            schedule = session.execute(
                select(Schedule)
                .where(Schedule.id == schedule_data.id)
            ).first()

            todo = session.execute(
                select(Todo)
                .where(Todo.schedule_id == schedule_data.id)
            ).all()

            for i in todo:
                session.delete(i)

            session.delete(schedule[0])
            session.commit()
            
            return 
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/student/modules/')
async def student_modules(auth_string: AuthString):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == auth_string.auth_string)).first()
    if user is not None:
        if user[0].status == 0:
            modules = session.execute(
                select(Module)
                .join(Class, Module.module_id == Class.module)
                .where(Class.student == user[0].username)
            ).scalars().all()
            
            return modules
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/student/modules/add/')
async def student_add_module(module_data: ModuleData):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == module_data.auth_string)).first()
    if user is not None:
        if user[0].status == 0:
            module_exists = session.execute(
                select(Module)
                .where(Module.module_id == module_data.module_id)
            ).first()

            if module_exists is None:
                raise HTTPException(status_code=418, detail="No such module.")

            class_exists = session.execute(
                select(Class)
                .where(Class.student == user[0].username)
                .where(Class.module == module_data.module_id)
            ).first()
            
            if class_exists is not None:
                raise HTTPException(status_code=418, detail="User already has module added.")
                
            new_class = Class(student=user[0].username, module=module_data.module_id)
            session.add(new_class)
            session.commit()
            return
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/student/modules/delete/')
async def student_delete_module(module_data: ModuleData):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == module_data.auth_string)).first()
    if user is not None:
        if user[0].status == 0:       
            class_exists = session.execute(
                select(Class)
                .where(Class.module == module_data.module_id)
                .where(Class.student == user[0].username)
            ).first()

            session.delete(class_exists[0])
            session.commit()
            return
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/modules/')
async def faculty_home(auth_string: AuthString):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == auth_string.auth_string)).first()
    if user is not None:
        if user[0].status == 1:
            modules = session.execute(
                select(Module)
                .where(Module.module_lead == user[0].username)
            ).scalars().all()
            
            return modules
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/modules/add/')
async def faculty_add_module(module_data: NewModuleData):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == module_data.auth_string)).first()
    if user is not None:
        if user[0].status == 1:
            module_exists = session.execute(
                select(Module)
                .where(Module.module_id == module_data.module_id)
            ).first()

            if module_exists is not None:
                raise HTTPException(status_code=418, detail="Module already exists!")
                
            new_class = Module(module_id=module_data.module_id, module_name=module_data.module_name, module_lead=user[0].username)
            session.add(new_class)
            session.commit()
            return
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")

@app.post('/faculty/modules/delete/')
async def faculty_delete_module(module_data: ModuleData):
    user = session.execute(
        select(User)
        .join(Auth, User.username == Auth.user)
        .where(Auth.auth_string == module_data.auth_string)).first()
    if user is not None:
        if user[0].status == 1:       
            class_exists = session.execute(
                select(Class)
                .where(Class.module == module_data.module_id)
            ).all()

            module = session.execute(
                select(Module)
                .where(Module.module_lead == User.username)
                .where(Module.module_id == module_data.module_id)
            ).first()

            session.delete(class_exists)
            session.delete(module)
            session.commit()
            return
        raise HTTPException(status_code=418, detail="Invalid user status.")
    raise HTTPException(status_code=418, detail="Invalid auth string.")
