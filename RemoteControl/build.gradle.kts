import java.util.Properties
import java.io.FileInputStream
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.Service

allprojects {

    repositories {
        jcenter()
        mavenCentral()
    }

}

plugins {
    id("org.hidetake.ssh") version "2.10.1"
}

subprojects {
    version = "1.0"
}

val getProps by extra {
    fun(propName: String): String {
        val propsFile = rootProject.file("local.properties")
        return if (propsFile.exists()) {
            val props = Properties()
            props.load(FileInputStream(propsFile))
            props[propName] as String
        } else {
            ""
        }
    }
}

val copyJarTask by extra {
    fun(project: Project, jarTask: TaskProvider<Jar>) {
        val copy = project.tasks.create("copy") {
            doLast {
                ssh.runSessions {
                    session(raspberry) {
                        put(jarTask.get().archiveFile.get().asFile, File("/home/pi/"))
                        logger.warn(execute("chmod +x ${jarTask.get().archiveFile.get().asFile.name}"))
                    }
                }
            }
        }
        copy.dependsOn(jarTask)
    }
}
var raspberry by extra {
    remotes.create("raspberry") {
        host = "192.168.1.38"
        user = "pi"
        password = getProps("password")
    }
}

val deployJarTask by extra {
    fun(project: Project, jarTask: TaskProvider<Jar>) {

        val deploy = project.tasks.create("deploy") {
            doLast {
                ssh.runSessions {
                    session(raspberry) {
                        try {
                            execute("sudo rm ${jarTask.get().archiveFile.get().asFile.name}")
                        } catch (e: Exception) {
                            logger.error(e.message)
                        }
                        put(jarTask.get().archiveFile.get().asFile, File("/home/pi/"))
                        logger.warn(execute("sudo pkill -f ${jarTask.get().archiveFile.get().asFile.name}"))
                        logger.warn(execute("chmod +x ${jarTask.get().archiveFile.get().asFile.name}"))
                        logger.warn(execute("sudo java -jar ${jarTask.get().archiveFile.get().asFile.name}"))
                    }
                }
            }
        }

        deploy.dependsOn(jarTask)
    }
}


fun Service.runSessions(action: RunHandler.() -> Unit) =
    run(delegateClosureOf(action))

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) =
    session(*remotes, delegateClosureOf(action))

fun SessionHandler.put(from: Any, into: Any) =
    put(hashMapOf("from" to from, "into" to into))
