package me.santio.nexus.command

import me.santio.nexus.ext.not
import org.incendo.cloud.caption.Caption
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.exception.parsing.ParserException
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.ArgumentParser.FutureArgumentParser

/**
 * An auto-serviceable interface that can be used to load in the cloud parser
 * @param <C> The sender type
 * @param <O> The class that's parsable
 */
interface Parser<O> : FutureArgumentParser<Source, O> {
    fun classType(): Class<*>?

    fun error(context: CommandContext<Source>, message: String): ParserException {
        context.sender().source().sendMessage(!"<error>$message")
        return NexusParserException(this.javaClass, context)
    }

    class NexusParserException internal constructor(
        clazz: Class<*>,
        context: CommandContext<*>
    ) : ParserException(clazz, context, Caption.of(""))
}
