/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:OptIn(
    ConsoleExperimentalApi::class, ConsoleInternalApi::class, ExperimentalCommandDescriptors::class
)

package net.mamoe.mirai.console.plugins.chat.command

import CommandSenderClient
import CommandSenderClientForward
import CommandSenderClientForward0
import kotlinx.coroutines.*
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandExecuteResult.*
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.command.descriptor.AbstractCommandValueParameter.StringConstant
import net.mamoe.mirai.console.command.descriptor.CommandReceiverParameter
import net.mamoe.mirai.console.command.descriptor.CommandValueParameter
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.command.parse.CommandCall
import net.mamoe.mirai.console.command.parse.CommandValueArgument
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugins.chat.command.ChatCommandConfig.enabled
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.console.util.cast
import net.mamoe.mirai.console.util.safeCast
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "net.mamoe.mirai.console.chat-command", name = "Chat Command", version = "0.5.4"
    )
) {
    @OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
    override fun onEnable() {
        ChatCommandConfig.reload()
        globalEventChannel().subscribeAlways(
            GroupMessageEvent::class,
            CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable)
            },
            priority = EventPriority.MONITOR,
        ) call@{
            if (!enabled) return@call
            val sender = kotlin.runCatching {
                this.toCommandSender()
            }.getOrNull() ?: return@call

            PluginMain.launch { // Async
                handleCommand(sender, message)
            }
        }
        globalEventChannel().subscribeAlways(
            FriendMessageEvent::class,
            CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable)
            },
            priority = EventPriority.MONITOR,
        ) call@{
            if (!enabled) return@call
            val sender = kotlin.runCatching {
                this.toCommandSender()
            }.getOrNull() ?: return@call

            PluginMain.launch { // Async
                handleCommand(sender, message)
            }
        }
        globalEventChannel().subscribeAlways(
            GroupMessageSyncEvent::class,
            CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable)
            },
            priority = EventPriority.MONITOR,
        ) call@{
            if (!enabled) return@call
            val sender = CommandSenderClient(this)
            PluginMain.launch { // Async
                handleCommand(sender, message)
            }
        }
    }

    private suspend fun handleCommand(sender0: CommandSender, message: MessageChain) {

        fun isDebugging(command: Command?): Boolean {/*
            if (command?.prefixOptional == false || message.content.startsWith(CommandManager.commandPrefix)) {
                if (MiraiConsoleImplementationBridge.loggerController.shouldLog("console.debug", SimpleLogger.LogPriority.DEBUG)) {
                    return true
                }
            }*/
            return false
        }

        var sender = sender0;
        if (message[1].content == "/help") {
            if (sender is MemberCommandSender) {
                sender = CommandSenderClientForward(sender)
            } else if (sender is CommandSenderClient) {
                sender = CommandSenderClientForward0(sender)
            }
        }
        when (val result = CommandManager.executeCommand(sender, message)) {

            is PermissionDenied -> {
                if (isDebugging(result.command)) {
                    sender.sendMessage(
                        "权限不足. <${result.call.caller.subject?.id}>" + "${CommandManager.commandPrefix}${result.command.primaryName} 需要权限 ${result.command.permission.id}."
                    )
                }
            }

            is IllegalArgument -> {
                result.exception.message?.let { sender.sendMessage(it) }
                // intercept()
            }

            is Success -> {
                //  intercept()
            }

            is ExecutionFailed -> {
                val owner = result.command.owner
                val (logger, printOwner) = when (owner) {
                    is JvmPlugin -> owner.logger to false
                    else -> MiraiConsole.mainLogger to true
                }
                logger.warning(
                    "Exception in executing command `$message`" + if (printOwner) ", command owned by $owner" else "",
                    result.exception
                )
                // intercept()
            }

            is Intercepted -> {
                if (isDebugging(result.command)) {
                    sender.sendMessage("指令执行被拦截, 原因: ${result.reason}")
                }
            }

            is UnmatchedSignature,
            -> {
                if ((ChatCommandConfig.replyUnresolvedCommandHelp && sender.hasPermission(result.command.permission)) || isDebugging(
                        result.command
                    )
                ) {
                    sender.sendMessage(
                        "参数不匹配, 你是否想执行: \n" + result.failureReasons.render(
                            result.command, result.call
                        )
                    )
                }
            }

            is UnresolvedCommand,
            -> {
                // noop
            }

            else -> {}
        }

    }

//    internal lateinit var commandListener: Listener<MessageEvent>
}


@OptIn(ExperimentalCommandDescriptors::class)
private fun List<UnmatchedCommandSignature>.render(command: Command, call: CommandCall): String {
    val list = this.filter lambda@{ signature ->
        if (signature.failureReason.safeCast<FailureReason.InapplicableValueArgument>()?.parameter is StringConstant) return@lambda false
        if (signature.signature.valueParameters.anyStringConstantUnmatched(call.valueArguments)) return@lambda false
        true
    }
    if (list.isEmpty()) {
        return command.usage
    }
    return list.joinToString("\n") { it.render(command) }
}

private fun List<CommandValueParameter<*>>.anyStringConstantUnmatched(arguments: List<CommandValueArgument>): Boolean {
    return this.zip(arguments).any { (parameter, argument) ->
        parameter is StringConstant && !parameter.accepts(argument, null)
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun UnmatchedCommandSignature.render(command: Command): String {
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") val usage =
        net.mamoe.mirai.console.internal.command.CommandReflector.generateUsage(command, null, listOf(this.signature))
    return usage.trim() + "    (${failureReason.render()})"
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun FailureReason.render(): String {
    return when (this) {
        is FailureReason.InapplicableArgument -> "参数类型错误"
        is FailureReason.InapplicableReceiverArgument -> "需要由 ${this.parameter.renderAsName()} 执行"
        is FailureReason.TooManyArguments -> "参数过多"
        is FailureReason.NotEnoughArguments -> "参数不足"
        is FailureReason.ResolutionAmbiguity -> "调用歧义"
        is FailureReason.ArgumentLengthMismatch -> {
            // should not happen, render it anyway.
            "参数长度不匹配"
        }
    }
}

@OptIn(ExperimentalCommandDescriptors::class)
internal fun CommandReceiverParameter<*>.renderAsName(): String {
    val classifier = this.type.classifier.cast<KClass<out CommandSender>>()
    return when {
        classifier.isSubclassOf(ConsoleCommandSender::class) -> "控制台"
        classifier.isSubclassOf(FriendCommandSenderOnMessage::class) -> "好友私聊"
        classifier.isSubclassOf(FriendCommandSender::class) -> "好友"
        classifier.isSubclassOf(MemberCommandSenderOnMessage::class) -> "群内发言"
        classifier.isSubclassOf(MemberCommandSender::class) -> "群成员"
        classifier.isSubclassOf(GroupTempCommandSenderOnMessage::class) -> "群临时会话"
        classifier.isSubclassOf(GroupTempCommandSender::class) -> "群临时好友"
        classifier.isSubclassOf(UserCommandSender::class) -> "用户"
        else -> classifier.simpleName ?: classifier.toString()
    }
}
