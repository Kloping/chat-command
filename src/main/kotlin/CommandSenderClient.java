import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.CommandSenderOnMessage;
import net.mamoe.mirai.console.permission.AbstractPermitteeId;
import net.mamoe.mirai.console.permission.PermitteeId;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageSyncEvent;
import net.mamoe.mirai.event.events.MessageSyncEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author github.kloping
 */
public class CommandSenderClient implements CommandSenderOnMessage<MessageSyncEvent> {
    private GroupMessageSyncEvent event;

    public CommandSenderClient(GroupMessageSyncEvent event) {
        this.event = event;
    }

    @Nullable
    @Override
    public Bot getBot() {
        return event.getBot();
    }

    @NotNull
    @Override
    public String getName() {
        return "client";
    }

    @Nullable
    @Override
    public Contact getSubject() {
        return event.getSubject();
    }

    @Nullable
    @Override
    public User getUser() {
        return event.getSender();
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull String s, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        event.getGroup().sendMessage(s);
        return null;
    }

    @Nullable
    @Override
    public Object sendMessage(@NotNull Message message, @NotNull Continuation<? super MessageReceipt<? extends Contact>> continuation) {
        event.getGroup().sendMessage(message);
        return null;
    }

    @NotNull
    @Override
    public MessageSyncEvent getFromEvent() {
        return event;
    }

    @NotNull
    @Override
    public PermitteeId getPermitteeId() {
        return new AbstractPermitteeId.ExactMember(event.getSubject().getId(), event.getSender().getId());
    }

    @NotNull
    @Override
    public CoroutineContext getCoroutineContext() {
        return event.getBot().getCoroutineContext();
    }
}
