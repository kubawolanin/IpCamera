/**
	 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.ipcamera.internal;

import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_DOORBELL;
import static org.openhab.binding.ipcamera.IpCameraBindingConstants.CHANNEL_MOTION_ALARM;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ipcamera.handler.IpCameraHandler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

public class DoorBirdHandler extends ChannelDuplexHandler {
	IpCameraHandler ipCameraHandler;

	public DoorBirdHandler(ThingHandler handler) {
		ipCameraHandler = (IpCameraHandler) handler;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String content = null;
		try {
			content = msg.toString();
			if (!content.isEmpty()) {
				ipCameraHandler.logger.trace("HTTP Result back from camera is \t:{}:", content);
			} else {
				return;
			}
			if (content.contains("doorbell:H")) {
				ipCameraHandler.setChannelState(CHANNEL_DOORBELL, OnOffType.valueOf("ON"));
			}
			if (content.contains("doorbell:L")) {
				ipCameraHandler.setChannelState(CHANNEL_DOORBELL, OnOffType.valueOf("OFF"));
			}
			if (content.contains("motionsensor:L")) {
				ipCameraHandler.setChannelState(CHANNEL_MOTION_ALARM, OnOffType.valueOf("OFF"));
				ipCameraHandler.firstMotionAlarm = false;
				ipCameraHandler.motionAlarmUpdateSnapshot = false;
			}
			if (content.contains("motionsensor:H")) {
				ipCameraHandler.motionDetected(CHANNEL_MOTION_ALARM);
			}

		} finally {
			ReferenceCountUtil.release(msg);
			content = null;
		}
	}
}