package com.cmiethling.mplex.device.api.fluidics;

import org.springframework.lang.NonNull;

import com.cmiethling.mplex.device.DeviceCommandException;
import com.cmiethling.mplex.device.DeviceMessageException;
import com.cmiethling.mplex.device.message.ResultMessage;

import lombok.Getter;

/**
 * Fetch the current gel pump pressure.
 */
@Getter
public final class ReadGelPumpPressureCommand extends AbstractFluidicsDeviceCommand {

    public static final String TOPIC = "ReadHighPressure";
    public static final String PRESSURE_PARAM = "pressure";

    private double pressure;

    /**
     * Creates a new command for use with the device interface.
     */
    public ReadGelPumpPressureCommand() {
        super(TOPIC);
    }

    @Override
    public void fromResultMessage(@NonNull final ResultMessage result)
            throws DeviceMessageException, DeviceCommandException {
        super.fromResultMessage(result);

        this.pressure = result.parameters().getRequiredDouble(PRESSURE_PARAM);
    }
}

