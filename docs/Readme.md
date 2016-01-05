# FaFram documentation

## SSHClient

SSHClient is based on the JSch library. Currently there are two SSHClients - _NodeSSHClient_ and _FuseSSHClient_ both implementing the 
abstract _SSHClient_ class.

The abstract SSHClient class has a set of common methods that both clients use and one abstract method:


	public abstract String executeCommand(String command, boolean supressLog) throws KarafSessionDownException,
			SSHClientException


that both clients implement in a different way.

The difference is in the exception handling and more details about this can be found in the respective implementations of the abstract method.

## Deployer

Deployer represents the main functionality of FaFram8 framework and it is using the Fuse- and NodeSSH- clients. The framework can be ran from 
the test in two ways:

First is to use the JUnit rule:


	@Rule
	public Fafram fafram = new Fafram();


Second is to trigger the start manually from the test (and tearDown from @After/@AfterClass)


	public class ExampleTest {
		private Fafram fafram;

		@Before
		public void init() {
			fafram = new Fafram();
			fafram.setup();
		}

		@Test
		public void dummyTest() {
		}

		@After
		public void tearDown() {
			fafram.tearDown();
		}
	}

There are two basic deployment structures: _local deployment_ and _remote deployment_.

### Local deployment

Local deployment can be used to run a single Fuse/A-MQ instance on your localhost. It gets the distribution zip file from your maven local 
repository (if no additional properties are specified). Then it does a list of steps necessary to set up the distribution (unzip, modify, 
run) and the whole workflow looks like this:

	Get the zip file
	Unzip into the target folder
	Modify the container
	Start the container
	Create fabric if specified
	Patch the distribution if specified
	<your test>
	Stop the container
	Delete the container directory if it's desired
	
### Remote deployment

Remote deployment todo rjakubco 

### System properties
The workflow or properties can be modified using system properties. Full list of system properties is

	host - Host IP address
	host.port - Host port
	host.user - Host SSH login
	host.password - Host SSH password
	fuse.user - Fuse user
	fuse.password - Fuse password
	fuse.group - Fuse artifact maven group id
	fuse.id - Fuse artifact maven artifact id
	fuse.version - Fuse version, for example 6.2.0.redhat-133
	fuse.zip - Fuse zip location
	start.wait.time - Karaf start wait time
	stop.wait.time - Karaf stop wait time
	provision.wait.time - Fabric provision wait time
	patch.wait.time - Patch install wait time
	keepFolder - Flag if the folder should be kept after the execution
	fafram.folder - Name of the working folder on remote host
	patch - Patch location
	fafram.working.directory - Special working directory on remote machine
	fafram.patch.dir - Patch directory location
	fafram.skip.default.user - Skip default user add
	fafram.patch.standalone - Patch standalone - useful only together with .withFabric() method
	fafram.skip.default.jvm.opts - Skip adding default JVM opts (xms=768M, xmx=1536M, permMem=768M, maxPermMem=1536M) 
	fafram.suppress.start - Testing purposes only - do not start fuse for the tests that do not need it
	fafram.archive.target - Target dir where the archived artifacts will be stored
	fafram.archive.pattern - Archive pattern equal to the jenkins archive pattern
	fafram.base.dir - base dir of the project - on localhost it's "" and on jenkins it's System.getenv("WORKSPACE")
	keep.os.resources - If "true" OpenStackProvisionManager will not release OS nodes after test
	broker.start.wait.time - broker start wait time

### Patches

Patcher class is used to prepare the patch zips to be installed. Currently the **patch** property accepts four ways how you can define the patch:

	-Dpatch=r2 - scans the <fafram.patch.dir> recursively for the patch zip name that contains the string "r2" and returns the first occurance.
	The patches are (hopefully) always sorted lexicographically, so r2 patch zip should be found sooner than r2p4 for example. It is possible 
	to use the string "r2,r2p4" where both patch zips will be returned and installed. At the same time it checks if the patch version matches
	the current version set by the fuse.version property.

	-Dpatch=latest - scans the <fafram.patch.dir>/latest folder and returns all the files in the folder. Again, they should be sorted so the right order should be preserved.
	
	-Dpatch=file:///home/file.zip - returns the zip file location
	
	-Dpatch=http://www.example.com/file.zip - returns the zip file location
	
### Container zip location

By default the FaFram8 framework gets the container zip from the local maven repository. First of all, 
it gets the maven local repository location using the maven invoker. For the maven invoker you need to have a maven binary in your **PATH** 
environment variable, or the **M2_HOME** system or environment property set. The FaFram8 framework then constructs the absolute path to the 
container zip file using the **fuse.group**, **fuse.id** and **fuse.version** properties.

This default behavior can be overriden with setting the **fuse.zip** property in this way:

	-Dfuse.zip=file:///home/file.zip - unzips the file directly from this location
	
	-Dfuse.zip=http://www.example.com/file.zip - downloads the file to the machine and then unzips it
	

### Fafram8 JUnit Test runner

You can annotate your JUnit test class with `@RunWith(FaframTestRunner.class)` to be able to use the `@Jira("xxx")` annotation for your test. 
This annotation will check the jira status and can be used to skip the test if the issue is not fixed. The test will be run if the jira is in 
"Resolved" or "Closed" state, otherwise the test will be skipped. The test runner also prints the name of the current test method before the
actual test execution.

### External configuration

OpenStack and JIRA configuration is done via the _fafram.properties_ file. Sample configuration file can be found in
_deployer/src/main/resources/fafram.properties_ where all supported values are defined. If you will want to change these values in your tests,
you can place your own fafram.properties file in your test resources (you don't necessary need to overwrite all the properties). This file is
then merged with our properties file (with your changes on top of the default values) and the final configuration is created and used.

### Tests

Tests can be found in the respective directory in the module directories. Tests are skipped by default and this behavior can be overriden 
using the **skip.test.fafram** property.

Example usage:

	cd deployer; mvn clean install -Dskip.test.fafram=false -Dtest=HostValidatorTest

## FaFram8 example usage

The only necessary thing to include in your pom.xml file is the dependency for the framework. However, if you want your test to download the
container automatically and use it, you need to include this in your pom.xml file aswell.

	<?xml version="1.0" encoding="UTF-8"?>
	<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
		<modelVersion>4.0.0</modelVersion>

		<groupId>com.test</groupId>
		<artifactId>test</artifactId>
		<version>1.0-SNAPSHOT</version>

		<properties>
			<fuse.group>org.jboss.fuse</fuse.group>
			<fuse.id>jboss-fuse-full</fuse.id>
			<fuse.version>6.2.0.redhat-133</fuse.version>
        </properties>

		<dependencies>
			<dependency>
				<groupId>org.jboss.fuse.qa</groupId>
				<artifactId>fafram8</artifactId>
				<version>1.0-SNAPSHOT</version>
				<scope>test</scope>
			</dependency>
			<dependency>
				<groupId>junit</groupId>
				<artifactId>junit</artifactId>
				<version>4.12</version>
				<scope>test</scope>
			</dependency>
			<dependency>
				<groupId>org.jboss.fuse</groupId>
				<artifactId>jboss-fuse-full</artifactId>
				<type>zip</type>
				<version>${fuse.version}</version>
				<!-- We want just the zip without all other transitive dependencies -->
				<exclusions>
					<exclusion>
						<groupId>*</groupId>
						<artifactId>*</artifactId>
					</exclusion>
				</exclusions>
			</dependency>
		</dependencies>

		<build>
			<plugins>
				<plugin>
					<groupId>org.apache.maven.plugins</groupId>
					<artifactId>maven-surefire-plugin</artifactId>
					<version>2.18.1</version>
					<configuration>
						<skipTests>false</skipTests>
						<systemPropertyVariables>
							<fuse.group>org.jboss.fuse</fuse.group>
							<fuse.id>${fuse.id}</fuse.id>
							<fuse.version>${fuse.version}</fuse.version>
						</systemPropertyVariables>
					</configuration>
				</plugin>
			</plugins>
		</build>

	</project>

In this example, we added the dependency for jboss-fuse-full zip file, so that the maven will download the zip file into our local repository before
the test execution. Using the maven-surefire-plugin we pass the **fuse.group**, **fuse.id**, **fuse.version** properties into the test, that are
later used to construct the full path to your local repository artifact path. Using these properties you can easily switch between versions
and even between the Fuse and A-MQ distributions.

There is no need to use this approach, because you can still use some other (previously downloaded) zip file and pass it to FaFram using the **fuse.zip** property.

## Checkstyle

We are using checkstyle! If you want to control your code, use the **checkstyle** system property.

	mvn clean install -Dcheckstyle

For further info about contributing see _Readme.md_ in the root directory of the project.
