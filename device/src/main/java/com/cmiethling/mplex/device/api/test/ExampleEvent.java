package com.cmiethling.mplex.device.api.test;

import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.api.AbstractDeviceEvent;
import com.cmiethling.mplex.device.api.DeviceEvent;
import com.cmiethling.mplex.device.message.EventMessage;
import com.cmiethling.mplex.device.message.Subsystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * This command can be used with the simulated hardware.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public final class ExampleEvent extends AbstractDeviceEvent {

    /**
     * Published topic for registering the event class.
     */
    public static final String TOPIC = "ExampleEvent";

    /**
     * Published parameter name.
     */
    public static final String DELAY_PARAMETER = "delay";

    /**
     * Published parameter name.
     */
    public static final String NAME_PARAMETER = "name";

    private int delay;
    private String name;

    /**
     * Creates a new event object.
     */
    public ExampleEvent() {
        super(Subsystem.TEST, TOPIC);
    }

    /**
     * This is needed after the Event was sent and now is "rebuild" for comparison. Usually this won't be needed, as we
     * only use the {@link DeviceEvent} and not the {@link EventMessage}.
     */
    @Override
    public EventMessage toEventMessage() {
        final var message = super.toEventMessage();

        if (this.delay != 0)// then there is a defined delay
            message.parameters().putInt(DELAY_PARAMETER, this.delay);
        message.parameters().putString(NAME_PARAMETER, this.name);
        return message;
    }

    @Override
    public void fromEventMessage(final @NonNull EventMessage event) throws DeviceMessageException {
        super.fromEventMessage(event);

        // if 0 then there is no defined delay
        this.delay = event.parameters().getInt(DELAY_PARAMETER).orElse(0);
        this.name = event.parameters().getString(NAME_PARAMETER).orElse(null);
    }
}

