package com.cmiethling.mplex.device.api.fluidics;

import com.cmiethling.mplex.device.message.RequestMessage;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class SetGelPumpCommand extends AbstractFluidicsDeviceCommand {

    public static final String TOPIC = "SetGelPump";
    public static final String ON_PARAM = "on";

    private boolean on;

    public SetGelPumpCommand() {
        super(TOPIC);
    }

    @Override
    public RequestMessage toRequestMessage() {
        final var request = super.toRequestMessage();
        request.parameters().putBoolean(ON_PARAM, this.on);
        return request;
    }
}
