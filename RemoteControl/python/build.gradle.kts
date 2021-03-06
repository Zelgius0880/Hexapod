import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service

val getProps = rootProject.extra["getProps"] as (String) -> String

plugins {
    id("org.hidetake.ssh")
    id("ru.vyarus.use-python") version "2.2.0"
}

var raspberry =  rootProject.extra["raspberry"] as Remote

val deploy = tasks.create("deploy") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm remote.py")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put("remote.py", File("/home/pi/"))
                logger.warn(execute("chmod +x remote.py"))
                logger.warn(execute("sudo LD_LIBRARY_PATH=/usr/lib PYTHONPATH=/usr/lib/python2.7/site-packages python remote.py"))
            }
        }
    }
}

val copy = tasks.create("copy") {
    doLast {
        ssh.runSessions {
            session(raspberry) {
                try {
                    execute("sudo rm remote.py")
                } catch (e: Exception) {
                    logger.error(e.message)
                }
                put("remote.py", File("/home/pi/"))
            }
        }
    }
}

fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))
