# FaFram examples

## Common mistakes

* If you don't specify any container, Fafram will create one root for you on your localhost with based on the system properties
* If you want to specify multiple containers, you need to explicitly define the root container - RootBuilder method .defaultRoot() will help you
* If you want to set up fabric, there are two options:
  * Default root (the one created automatically by Fafram): use .withFabric() in Fafram class, e.g. `new Fafram().withFabric()`
  * Custom root: use .withFabric() in RootBuilder class, e.g. `RootBuilder.defaultRoot().withFabric()`

## Examples

### One default root on localhost

```
@Rule
public Fafram fafram = new Fafram();
```

This will set up one root container on local host using default system properties - you can see the values in the *SystemProperty* class.

### One default root with child container on localhost

**Main thing to remember is that the default container will be created only when no containers are specified.** If you want to set up the default
container together with other containers, you need to explicitly define this behavior.

```
@Rule
public Fafram fafram = new Fafram().containers(
	RootContainer.builder().defaultRoot().withFabric().build(),
	ChildContainer.builder().name("child").parentName("root").build()
);
```

Using the same approach you can define more child containers and even SSH containers.

### Two roots on remote using a template

```
private static final Container TEMPLATE = RootContainer.builder()
			.profiles("gateway-mq")
			.commands("profile-create template")
			.withFabric()
			.build();
	private Container root1 = RootContainer.builder(TEMPLATE).name("root").commands("profile-create root1").build();
	private Container root2 = RootContainer.builder(TEMPLATE).name("root2").profiles("gateway-http").build();
	@Rule
	public Fafram fafram = new Fafram().fuseZip("http://path/to/fuse.zip").provider(FaframProvider.OPENSTACK)
			.containers(root1, root2);
```

This will first create a template container. In the node we specify the host "openstack" and later will the Fafram framework spawn a new openstack
node and set the actual IP to the node object. In this template container we specify the common attributes of both root containers, so in this case
both containers will be created with profile _gateway-mq_ and after spawning there will be command _profile-create template_ executed.

Next we define the real containers. As you can see, you can use the existing container instance in the _builder()_ method and all attributes will
be reused in the new containers.

*NOTE*: These attributes will not be overriden! The new attributes will be appended to the existing ones, so in this example the container _root2_
will have two profiles: gateway-mq (inherited from template) and gateway-http.

In the rule definition, you can see, that we specify that the containers will be created in fabric mode, and we will create the container from
http://path/to/fuse.zip. Using .provider(FaframProvider.OPENSTACK) we specify that we want to spawn new openstack machines for this scenario.

### Root and SSH container using static IPs

```
public Fafram fafram = new Fafram().fuseZip("http://path/to/fuse.zip").containers(
			RootContainer.builder().name("myroot").withFabric().node(
					host("1.2.3.4")
							.port(22)
							.username("myuser")
							.password("mypassword")
							.build()
			).build(),
			SshContainer.builder().name("ssh").node("5.6.7.8", 22, "nextUser", "nextPassword")
			.parentName("myroot").build()
	);
```

In this scenario, we want to create one root container named "myroot" on host "1.2.3.4", the host credentials are "myuser/mypassword". Next we want
to create a SSH container on host "5.6.7.8", where the credentials are "nextuser/nextpassword". As you can see, you don't need to specify the node
port, as it takes the default port from the system property host.port (defaults to 22).

*REMEMBER*: You always need to specify the parent of child and ssh containers. There are two ways of doing this: _.parentName(String name)_ method
for the cases when you have just the parent's name, or _parent(Container c)_ when you already have the object representation of root container.

### Wait for URL to return HTTP status 200

```java
@Rule
Fafram fafram = new Fafram();

final HttpClient client = new DefaultHttpClient();
final HttpGet request = new HttpGet("http://localhost:8181/whatever");

Callable<Response<HttpEntity>> c = new Callable<Response<HttpEntity>>() {
    @Override
    public Response<HttpEntity> call() throws Exception {
      // val comes from project lombok / automagically figure out type
      val result = client.execute(request);
      log.info("http status line: {}", result.getStatusLine());
      if (200 == result.getStatusLine().getStatusCode()) {
        return Response.success(result.getEntity());
      }
      // apache http client requires to consume response data
      EntityUtils.consume(result.getEntity());
      return Response.timeOut();
    }
};
final long secondsTimeout = 120;
Response<HttpEntity> response = fafram.waitFor(c, secondsTimeout);

if (!response.getSuccess()) {
  fail("Deployed war didn't respond '200' in 120 s.");
}
val responseString = IOUtils.toString(response.getData().getContent());
System.out.println("response: " + responseString);

```
