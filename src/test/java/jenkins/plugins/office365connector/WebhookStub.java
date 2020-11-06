package jenkins.plugins.office365connector;


public class WebhookStub extends Webhook {

  public WebhookStub(String url) {
    super(url);
  }

  public static class DescriptorImplStub extends Webhook.DescriptorImpl {

    @Override
    public synchronized void load() {
    }
  }
}
