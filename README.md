[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2804cd619dde437a883da48ad5c283bc)](https://www.codacy.com/app/coder103/authzforce-ce-core-pdp-api?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=authzforce/core-pdp-api&amp;utm_campaign=Badge_Grade)

# AuthZForce Core PDP API
High-level API for using AuthZForce PDP engine and implementing PDP engine extensions: attribute datatypes, functions, policy/rule combining algorithms, attribute providers, policy providers, XACML Request/Result filters, etc.

## Support

If you are experiencing any problem with this project, you may report it on the [OW2 Issue Tracker](https://jira.ow2.org/browse/AUTHZFORCE/).
Please include as much information as possible; the more we know, the better the chance of a quicker resolution:

* Software version
* Platform (OS and JDK)
* Stack traces generally really help! If in doubt include the whole thing; often exceptions get wrapped in other exceptions and the exception right near the bottom explains the actual error, not the first few lines at the top. It's very easy for us to skim-read past unnecessary parts of a stack trace.
* Log output can be useful too; sometimes enabling DEBUG logging can help;
* Your code & configuration files are often useful.

If you wish to contact the developers for other reasons, use [Authzforce contact mailing list](http://scr.im/azteam).


## Contributing
### Contribution Rules
1. No SNAPSHOT dependencies on "develop" and obviously "master" branches

### Releasing
1. From the develop branch, prepare a release (example using a HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=3128 jgitflow:release-start
</code></pre>
1. Update the CHANGELOG according to keepachangelog.com.
1. To perform the release (example using a HTTP proxy):
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=3128 jgitflow:release-finish
</code></pre>
    If, after deployment, the command does not succeed because of some issue with the branches. Fix the issue, then re-run the same command but with 'noDeploy' option set to true to avoid re-deployment:
<pre><code>
    $ mvn -Dhttps.proxyHost=proxyhostname -Dhttps.proxyPort=3128 -DnoDeploy=true jgitflow:release-finish
</code></pre>
    If the command fails because of a gpg error such as "no gpg-agent available to this session" or "no pinentry", make sure you have installed a pinentry program (e.g. with package `pinentry-gnome3`), and that a gpg-agent is running with this pinentry program, for example:
 <pre><code>
    $ gpg-agent --daemon --pinentry-program /usr/bin/pinentry-gnome3
</code></pre>
    Then re-run the mvn command as above.
1. Connect and log in to the OSS Nexus Repository Manager: https://oss.sonatype.org/
1. Go to Staging Profiles and select the pending repository authzforce-*... you just uploaded with `jgitflow:release-finish`
1. Click the Release button to release to Maven Central.

More info on jgitflow: http://jgitflow.bitbucket.org/
