# Office-365-Connector
Office 365 Connector plugin for Jenkins

Started with a fork of the Workplace plugin:

https://github.com/Outconn2016/workplace-plugin

Jenkins Instructions
Install this plugin on your Jenkins server
Configure it in your Jenkins job and add webhook URL obtained from office 365 connector.

Developer instructions
Install Maven and JDK. This was last build with Maven 3.2.5 and OpenJDK 1.7.0_75 on KUbuntu 14.04.

Run unit tests

mvn test
Create an HPI file to install in Jenkins (HPI file will be in target/slack.hpi).

mvn package
