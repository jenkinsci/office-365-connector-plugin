[![Github build](https://github.com/jenkinsci/office-365-connector-plugin/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/jenkinsci/office-365-connector-plugin/actions/workflows/build.yml)
[![Build Status](https://ci.jenkins.io/job/Plugins/job/office-365-connector-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/office-365-connector-plugin/job/master/)

[![Coverage Status](https://codecov.io/gh/jenkinsci/office-365-connector-plugin/branch/master/graph/badge.svg)](https://codecov.io/github/jenkinsci/office-365-connector-plugin)
[![Popularity](https://img.shields.io/jenkins/plugin/i/Office-365-Connector.svg)](https://plugins.jenkins.io/Office-365-Connector)


# Office-365-Connector

Office 365 Connector plugin for Jenkins

Plugin is used to send actionable messages in [Outlook](http://outlook.com), [Office 365 Groups](https://support.office.com/article/Learn-about-Office-365-Groups-b565caa1-5c40-40ef-9915-60fdb2d97fa2), and [Microsoft Teams](https://products.office.com/microsoft-teams/group-chat-software).

## Screenshots

### Jenkins configuration

![Configuration](https://github.com/jenkinsci/office-365-connector-plugin/raw/master/.README/config.png)

### Jenkins global configuration

![GlobalConfiguration](.README/globalconfig.png?raw=true)

#### Global configuration values used as default in jobs

![GlobalConfigurationDefault](.README/globalconfigdefault.png?raw=true)

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

The plugin can also be configured through global settings under Jenkins -> Manage Jenkins -> Configure System.
The values in the global settings will be used as default when using the plugin for a job.
These settings can then be overridden in the job. Changing the values in the global settings will however not
update any settings in an existing job.

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

    // Webhook Macro Configuration
    configure {
        // Example: Conditioning webhook trigger on build parameter 'version' being equal to 'latest'
        // Templates are defined as token macros https://github.com/jenkinsci/token-macro-plugin
        it / 'properties' / 'jenkins.plugins.office365connector.WebhookJobProperty' / 'webhooks' / 'jenkins.plugins.office365connector.Webhook' / 'macros' << 'jenkins.plugins.office365connector.model.Macro' {
          template('${ENV, var="version"}')
          value('latest')
        }
    }
}
```
### DSL Pipeline Job

```groovy
pipeline('Example Job Name'){
        // configure office365connector plugin using DSL configure for flow-definition
        configure { project ->
                       project / 'properties' << 'jenkins.plugins.office365connector.WebhookJobProperty' {
                       webhooks {
                           'jenkins.plugins.office365connector.Webhook' {
                               name("Office 365 Team channel notifications")
                               url("${Your webhook URL that needs to configure ( teams channel / outlook )}")
                               startNotification(false)
                               notifySuccess(false)
                               notifyAborted(false)
                               notifyNotBuilt(false)
                               notifyUnstable(false)
                               notifyFailure(true)
                               notifyBackToNormal(false)
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
            name: 'Office 365',
            startNotification: true,
            url: 'https://outlook.office.com/webhook/123456...'
        ]])
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

### Pipeline post section with Adaptive Cards

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
            office365ConnectorSend webhookUrl: "https://prod.westeurope.logic.azure.com:443/workflows...",
              message: 'Something went wrong', 
              status: 'Failure',
              adaptiveCards: true
        }
    }
}
````

### Global Configuration in Init Hook

Jenkins has the capability to execute Groovy scripts on launch by placing them in `$JENKINS_HOME/init.groovy.d`.
The following script will configure the Office 365 Connector globally every time Jenkins starts:

```groovy
import jenkins.model.Jenkins
import jenkins.plugins.office365connector.Webhook.DescriptorImpl

o365Connectors = Jenkins.get().getExtensionList(DescriptorImpl)

if (o365Connectors.size() == 0) {
  throw new ClassNotFoundException('The Office 365 Connector Plugin must be installed to be configured')
} else {
  o365Connector = o365Connectors[0]
}

String o365Url = 'https://example.webhook.office.com/webhookb2/07386f1b-1bc6-499f-ab7f-c9cf5e530cad@8f83d7b1-53ef-4906-a98e-9b8c4c3405b6/appId/852d8dec9176427b91f3658afb9e2513/9116b5aa-2a47-4248-88c2-41ef7340c222'
String o365Name = 'O365_Webhook'

o365Connector.setGlobalUrl(o365Url)
o365Connector.setGlobalName(o365Name)
```

## Documentation

You may find useful below link if you like to contribute and add new feature:

- [Office 365 Connectors for Microsoft Teams](https://docs.microsoft.com/microsoftteams/platform/concepts/connectors/connectors)
- [Actionable messages](https://docs.microsoft.com/outlook/actionable-messages/)
- [Support for Markdown](https://docs.microsoft.com/flow/approvals-markdown-support)
- [Adding connector to Microsoft Teams](https://docs.microsoft.com/microsoftteams/platform/concepts/connectors/connectors-using)
- [Office365 test dev account](https://developer.microsoft.com/office/dev-program)
- [Office365ConnectorSend parameters](https://jenkins.io/doc/pipeline/steps/Office-365-Connector/)
