
# Developer notes

## Development

### Recommended setup
1. Install Eclipse IDE for Eclipse Committers 2024-03
2. Install CDT
3. Install JDK 21
4. Set up Eclipse and PDE launch config to use your JDK 21 install
5. Import repo as 'Existing Maven Project'
6. Activate latest target version (in com.googlecode.cppchecklipse.target/)

### build

TODO command

### run tests locally

TODO command

### testing plugin in eclipse

If you have set up everything as recommended above, all you need to do to test locally is to create a run config.

The run config should be an Eclipse Application launch that is set to 'run a product'.
Under the plug-ins tab you should set it to launch with 'All workspace and enabled target Plug-ins', and click 'validate plug-ins' before running.

With the run config create you should be able to simply press 'run' and test out the plug-in. Note that you will need to set the path to your cppcheck executable in the test environment for the analysis to be able to run.

## release process

### version

 * Update version in the files see f260c53
   TODO right now this is done manually can it be done more automated?
 * create a tag in the repo
 * create a release in github

### updating files

download artifact "cppcheclipse-repository" from the github action "Java CI"
unzip the artifact and upload files to https://files.cppchecksolutions.com/cppcheclipse/ folder

### publish plugin in marketplace

to make sure that proper version info etc is shown in marketplace.
https://marketplace.eclipse.org
login
goto cppcheclipse plugin
click on "Edit" button at the top
Add version
