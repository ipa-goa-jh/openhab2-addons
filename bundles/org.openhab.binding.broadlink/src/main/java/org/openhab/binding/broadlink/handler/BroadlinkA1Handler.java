/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.broadlink.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.*;
import org.openhab.binding.broadlink.internal.*;
import org.slf4j.LoggerFactory;

/**
 * Handles the A1 environmental sensor.
 *
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public class BroadlinkA1Handler extends BroadlinkBaseThingHandler {

    public BroadlinkA1Handler(Thing thing) {
        super(thing, LoggerFactory.getLogger(BroadlinkA1Handler.class));
    }

    protected boolean getStatusFromDevice() {
        thingLogger.logTrace("A1 getStatusFromDevice");
        byte payload[];
        payload = new byte[16];
        payload[0] = 1;

        try {
            byte message[] = buildMessage((byte) 0x6a, payload);

            byte[] response = sendAndReceiveDatagram(message, "A1 device status");
            if (response == null) {
                thingLogger.logError("Got nothing back while getting device status");
                return false;
            }
            byte decryptResponse[] = decodeDevicePacket(response);
            float temperature = (float) ((double) (decryptResponse[4] * 10 + decryptResponse[5]) / 10D);
            thingLogger.logTrace("A1 getStatusFromDevice got temperature {}", temperature);

            updateState("temperature", new DecimalType(temperature));
            updateState("humidity", new DecimalType((double) (decryptResponse[6] * 10 + decryptResponse[7]) / 10D));
            updateState("light", ModelMapper.getLightValue(decryptResponse[8]));
            updateState("air", ModelMapper.getAirValue(decryptResponse[10]));
            updateState("noise", ModelMapper.getNoiseValue(decryptResponse[12]));
            return true;
        } catch (Exception ex) {
            thingLogger.logError("Failed while getting device status", ex);
            return false;
        }
    }

    protected boolean onBroadlinkDeviceBecomingReachable() {
        return getStatusFromDevice();
    }
}
