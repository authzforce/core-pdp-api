[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2804cd619dde437a883da48ad5c283bc)](https://www.codacy.com/app/coder103/authzforce-ce-core-pdp-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=authzforce/core-pdp-api&amp;utm_campaign=Badge_Grade)
[![Javadocs](http://javadoc.io/badge/org.ow2.authzforce/authzforce-ce-core-pdp-api.svg)](http://javadoc.io/doc/org.ow2.authzforce/authzforce-ce-core-pdp-api)

# AuthzForce Core PDP API
High-level API for using AuthzForce PDP engine and implementing PDP engine extensions: attribute datatypes, functions, policy/rule combining algorithms, attribute providers, policy providers, XACML Request/Result filters, etc.

## Support

If you are experiencing any problem with this project, you may report it on the GitHub Issues.
Please include as much information as possible; the more we know, the better the chance of a quicker resolution:

* Software version
* Platform (OS and JDK)
* Stack traces generally really help! If in doubt include the whole thing; often exceptions get wrapped in other exceptions and the exception right near the bottom explains the actual error, not the first few lines at the top. It's very easy for us to skim-read past unnecessary parts of a stack trace.
* Log output can be useful too; sometimes enabling DEBUG logging can help;
* Your code & configuration files are often useful.

If you wish to contact the developers for other reasons, use [AuthzForce contact mailing list](http://scr.im/azteam).

## Known issues
### Class not found: com.sun.mail.XXX
If you need to use XACML RFC822Name datatype, you need to add an actual implementation of JavaMail API as dependency (must match the version of `javax.mail-api` dependency in the [POM](pom.xml) ), such as:

```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.0</version>
</dependency>
```