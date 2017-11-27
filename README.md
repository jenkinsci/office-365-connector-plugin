# Office-365-Connector
Office 365 Connector plugin for Jenkins

Plugin is used to send actionable messages in [Outlook](http://outlook.com), [Office 365 Groups](https://support.office.com/en-us/article/Learn-about-Office-365-Groups-b565caa1-5c40-40ef-9915-60fdb2d97fa2), and [Microsoft Teams](https://products.office.com/en-us/microsoft-teams/group-chat-software).

[Read more about actionable messages](https://docs.microsoft.com/en-us/outlook/actionable-messages/)

## Screenshots

Configuration:
![Configuration](https://github.com/olegfeferman/office-365-connector-plugin/raw/master/.README/config.png)

Regular notifications:
![Regular Start Notification](https://github.com/olegfeferman/office-365-connector-plugin/raw/master/.README/regularStart.png)
![Regular Finish Notification](https://github.com/olegfeferman/office-365-connector-plugin/raw/master/.README/regularFinish.png)

Compact notifications:
![Compact Start Notification](https://github.com/olegfeferman/office-365-connector-plugin/raw/master/.README/compactStart.png)
![Compact Finish Notification](https://github.com/olegfeferman/office-365-connector-plugin/raw/master/.README/compactFinish.png)

## Jenkins Instructions

0. Install this plugin on your Jenkins server

0. Configure it in your Jenkins job and add webhook URL obtained from office 365 connector.

## Developer instructions
Install Maven and JDK. This was last build with Maven 3.2.5 and OpenJDK 1.7.0_75 on KUbuntu 14.04.

Run unit tests

`mvn test`

Create an HPI file to install in Jenkins (HPI file will be in target/slack.hpi).

`mvn package`
