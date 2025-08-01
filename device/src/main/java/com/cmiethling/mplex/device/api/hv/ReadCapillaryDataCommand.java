package com.cmiethling.mplex.device.api.hv;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceCommandException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.RequestMessage;
import com.cmiethling.mplex.device.message.ResultMessage;

import lombok.Getter;
import lombok.Setter;

/**
 * Fetch the data for one capillary.
 */
@Getter
public final class ReadCapillaryDataCommand extends AbstractHighVoltageDeviceCommand {

    public static final String TOPIC = "ReadCapillaryData";
    public static final String INDEX_PARAM = "index";
    public static final String CAP_STATE_RESULT = "capState";
    public static final String BAD_RUNS_RESULT = "badRuns";

    @Setter
    private int index;

    private CapillaryState capState;
    private int badRuns;

    /**
     * Creates a new command for use with the device interface.
     */
    public ReadCapillaryDataCommand() {
        super(TOPIC);
    }

    @Override
    public RequestMessage toRequestMessage() {
        final var message = super.toRequestMessage();
        message.parameters().putInt(INDEX_PARAM, this.index);
        return message;
    }

    @Override
    public void fromResultMessage(@NonNull final ResultMessage result)
            throws DeviceMessageException, DeviceCommandException {
        super.fromResultMessage(result);

        try {
            this.capState = CapillaryState.ofValue(result.parameters().getRequiredInt(CAP_STATE_RESULT));
        } catch (final IllegalArgumentException ex) {
            throw new DeviceMessageException("Invalid capState", ex);
        }

        this.badRuns = result.parameters().getRequiredInt(BAD_RUNS_RESULT);
    }
}

