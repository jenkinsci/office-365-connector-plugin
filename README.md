[![Travis Status](https://img.shields.io/travis/jenkinsci/office-365-connector-plugin/master.svg?label=Travis%20bulid)](https://travis-ci.org/jenkinsci/office-365-connector-plugin)
[![Shippable Status](https://api.shippable.com/projects/5a8bda80d0386507000ebf97/badge?branch=master&label=Shippable%20build)](https://app.shippable.com/github/jenkinsci/office-365-connector-plugin/dashboard)
[![Popularity](https://img.shields.io/jenkins/plugin/i/Office-365-Connector.svg)](https://plugins.jenkins.io/Office-365-Connector)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1fab6aea594f49928b80bfe55a81357c)](https://app.codacy.com/app/damianszczepanik/office-365-connector-plugin?utm_source=github.com&utm_medium=referral&utm_content=jenkinsci/office-365-connector-plugin&utm_campaign=Badge_Grade_Settings)
[![Coverage Status](https://img.shields.io/codecov/c/github/jenkinsci/office-365-connector-plugin/master.svg?label=Unit%20tests%20coverage)](https://codecov.io/github/jenkinsci/office-365-connector-plugin)
[![Vulnerabilities](https://snyk.io/test/github/jenkinsci/office-365-connector-plugin/badge.svg)](https://app.snyk.io/org/damianszczepanik/project/c78d3196-4d6a-4a74-a217-6f6bc5b2f6ac)

# Office-365-Connector
Office 365 Connector plugin for Jenkins

Plugin is used to send actionable messages in [Outlook](http://outlook.com), [Office 365 Groups](https://support.office.com/en-us/article/Learn-about-Office-365-Groups-b565caa1-5c40-40ef-9915-60fdb2d97fa2), and [Microsoft Teams](https://products.office.com/en-us/microsoft-teams/group-chat-software).

## Screenshots

### Jenkins configuration
![Configuration](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/config.png)

### Microsoft Teams
#### With Jenkins plugin
![Teams](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/teams.png)

#### With generic webhook connection
![Webhook](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/webhook.png)

### Microsoft Outlook
![Outlook](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/outlook.png)

## Jenkins Instructions

1. Install this plugin on your Jenkins server
2. Configure it in your Jenkins job and add webhook URL obtained from office 365 connector.
  
## Examples

### DSL
```groovy
job('Example Job Name') {
    description 'Example description'
    properties {
        office365ConnectorWebhooks {
            webhooks {
                webhook {
                    name('Example Webhook Name')
                    url('https://outlook.office.com/webhook/123456...')
                    startNotification(false)
                    notifySuccess(true)
                    notifyAborted(false)
                    notifyNotBuilt(false)
                    notifyUnstable(true)
                    notifyFailure(true)
                    notifyBackToNormal(true)
                    notifyRepeatedFailure(false)
                    timeout(30000)
                }
            }
        }
    }
}
```

### Pipeline properties
```groovy
pipeline {

    agent any

    options {
        office365ConnectorWebhooks([[
                    startNotification: true,
                        url: 'https://outlook.office.com/webhook/123456...'
            ]]
        )
    }

    stages {
        stage('Init') {
            steps {
                echo 'Starting!'
            }
        }
    }
}
```

### Pipeline step
```groovy
stage('Upload') {
    steps {
        // some instructions here
        office365ConnectorSend webhookUrl: 'https://outlook.office.com/webhook/123456...',
            message: 'Application has been [deployed](https://uat.green.biz)',
            status: 'Success'            
    }
}
```

### Pipeline post section
```groovy
pipeline {

    agent any

    stages {
        stage('Init') {
            steps {
                echo 'Hello!'
            }
        }
    }

    post {
        failure {
            office365ConnectorSend webhookUrl: "https://outlook.office.com/webhook/123456...",
                factDefinitions: [[name: "fact1", template: "content of fact1"],
                                  [name: "fact2", template: "content of fact2"]]
        }
    }
}
```

## Documentation

You may find useful below link if you like to contribute and add new feature:
- [Office 365 Connectors for Microsoft Teams](https://docs.microsoft.com/pl-pl/microsoftteams/platform/concepts/connectors/connectors)
- [Actionable messages](https://docs.microsoft.com/en-us/outlook/actionable-messages/)
- [Support for Markdown](https://docs.microsoft.com/en-us/flow/approvals-markdown-support)
- [Adding connector to Microsoft Teams](https://docs.microsoft.com/pl-pl/microsoftteams/platform/concepts/connectors/connectors-using)
- [Office365 test dev account](https://developer.microsoft.com/office/dev-program)
- [Office365ConnectorSend parameters](https://jenkins.io/doc/pipeline/steps/Office-365-Connector/)
