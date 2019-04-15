[![Build Travis](https://img.shields.io/travis/jenkinsci/office-365-connector-plugin/master.svg)](https://travis-ci.org/jenkinsci/office-365-connector-plugin)

[![Popularity](https://img.shields.io/jenkins/plugin/i/Office-365-Connector.svg)](https://plugins.jenkins.io/Office-365-Connector)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1fab6aea594f49928b80bfe55a81357c)](https://app.codacy.com/app/damianszczepanik/office-365-connector-plugin?utm_source=github.com&utm_medium=referral&utm_content=jenkinsci/office-365-connector-plugin&utm_campaign=Badge_Grade_Settings)
[![Coverage Status](https://img.shields.io/codecov/c/github/jenkinsci/office-365-connector-plugin/master.svg?label=Unit%20tests%20coverage)](https://codecov.io/github/jenkinsci/office-365-connector-plugin)

# Office-365-Connector
Office 365 Connector plugin for Jenkins

Plugin is used to send actionable messages in [Outlook](http://outlook.com) , [Office 365 Groups](https://support.office.com/en-us/article/Learn-about-Office-365-Groups-b565caa1-5c40-40ef-9915-60fdb2d97fa2), and [Microsoft Teams](https://products.office.com/en-us/microsoft-teams/group-chat-software).

[Read more about actionable messages](https://docs.microsoft.com/en-us/outlook/actionable-messages/)

## Screenshots

![Configuration](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/config.png)

![Message](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/message.png)

## Jenkins Instructions

0. Install this plugin on your Jenkins server

0. Configure it in your Jenkins job and add webhook URL obtained from office 365 connector.

## Developer instructions
Install Maven and JDK. This was last build with Maven 3.2.5 and OpenJDK 1.7.0_75 on KUbuntu 14.04.

Run unit tests

`mvn test`

Create an HPI file to install in Jenkins (HPI file will be in target/slack.hpi).

`mvn package`
