# **SHaME**

## **Introduction**
Student Help and Moral Encouragement \(SHaME) is an Android application designed to help students and faculty collate deadlines on one platform, making it easier to track.

Faculty are able to see the number of students enrolled that have completed each of their set deadlines, allowing them to extend the deadline if they feel not enough students have completed it.

Students are able to see a chronologically sorted list of deadlines set by the modules they have subscribed to, and can mark deadlines as completed, enabling them to better prioritize their time and workload.

## **Installation**
### **Android Application**
The Android project can be found in the `Infosys1D` folder in this repo, and can be built as-is.

### **Backend**
How to run:
1. Via provided virtual environment:
	- All required dependencies have been added in the included virtual environment
	- Run `.\venv\Scripts\activate` on Windows to activate the virtual environment
	- Run `source myvenv/bin/activate` on *nix OSes to activate the virtual environment
	- Start the backend from within the virtual environment with `uvicorn backend:app`

2. From your own Python installation:
	- pip install the provided requirements.txt file to install all required dependencies
	- Start the backend with 'uvicorn backend:app'

The backend should now be listening on `localhost` port `8000`
`localhost:8000/docs` can be accessed to see a list of available endpoints for use from the API

## **Notes**
To run on servers other than `localhost`, two values have to be updated:
- In the Android project, update `API_AUTHORITY` in `res/values/strings.xml` to the new desired endpoint.
- When starting the `uvicorn` application, specify the `--host` \(and `--port`, if necessary), e.g.:
```
uvicorn --host newaddress.com --port 422 backend:app
```

The backend and application should now be running on the new specified host / port.
