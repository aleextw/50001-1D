How to run:
1. Via provided virtual environment:
	- All required dependencies have been added in the included virtual environment
	- Run '.\venv\Scripts\activate' on Windows to activate the virtual environment
	- Run 'source myvenv/bin/activate' on *nix OSes to activate the virtual environment
	- Start the backend from within the virtual environment with 'uvicorn backend:app'

2. From your own Python installation:
	- pip install the provided requirements.txt file to install all required dependencies
	- Start the backend with 'uvicorn backend:app'

The backend should now be listening on localhost port 8000
localhost:8000/docs can be accessed to see a list of available endpoints for use from the API