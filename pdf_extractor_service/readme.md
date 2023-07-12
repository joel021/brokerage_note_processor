<h1>Setup</h1>

<b>Create environment</b>

$python -m venv venv

- create config.py as <a href="config.example.py">config.example.py</a>

**If using linux, install mysql client**

$sudo apt-get install libmysqlclient-dev


<h1>Tests</h1>

<b>Run all tests</b><br />
$python -m unittest discover

<b>Run only one test class</b><br />

<i>$python -m unittest class</i><br /><br />
<i>$python -m unittest tests.tests_extraction.test_extractor_service.TestOperationsExtractor</i><br /><br />
<i>$python -m unittest tests.tests_manager.test_operation_controller.TestOperationController</i><br /><br />



