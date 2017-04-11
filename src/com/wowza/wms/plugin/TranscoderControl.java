/*
 * This code and all components (c) Copyright 2006 - 2016, Wowza Media Systems, LLC. All rights reserved.
 * This code is licensed pursuant to the Wowza Public License version 1.0, available at www.wowza.com/legal.
 */
package com.wowza.wms.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wowza.util.StringUtils;
import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.module.ModuleBase;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderControl;

public class TranscoderControl extends ModuleBase
{

	private class Controller implements ILiveStreamTranscoderControl
	{

		@Override
		public boolean isLiveStreamTranscode(String transcoder, IMediaStream stream)
		{
			if (stream.isTranscodeResult())
			{
				return false;
			}
			
			if (namesRegex != null) {
				Matcher m = namesRegex.matcher(stream.getName());
				if (m.matches()) {
					return matchAllow;
				} else {
					return noMatchAllow;
				}
			}

			logger.info(MODULE_NAME + ".isLiveStreamTranscode [" + transcoder + " : " + stream.getName() + " : " + names + "]", stream);
			if (names.equals("*"))
			{
				logger.info(MODULE_NAME + ".isLiveStreamTranscode [" + transcoder + " : " + stream.getName() + " :  names is wildcard. returning " + matchAllow + "]", stream);
				return matchAllow;
			}
			if (StringUtils.isEmpty(names))
			{
				logger.info(MODULE_NAME + ".isLiveStreamTranscode [" + transcoder + " : " + stream.getName() + " : names is empty. returning " + noMatchAllow + "]", stream);
				return noMatchAllow;
			}

			String[] namesArray = names.split(",");
			for (String name : namesArray)
			{
				if (stream.getName().matches(name.trim()))
				{
					logger.info(MODULE_NAME + ".isLiveStreamTranscode [" + transcoder + " : " + stream.getName() + " : match found : " + name.trim() + " : returning " + matchAllow + "]", stream);
					return matchAllow;
				}
			}
			logger.info(MODULE_NAME + ".isLiveStreamTranscode [" + transcoder + " : " + stream.getName() + " : No match found : returning " + noMatchAllow + "]", stream);
			return noMatchAllow;
		}
	}

	public static final String MODULE_NAME = "TranscoderControl";
	public static final String PROP_NAME_PREFIX = "transcoderControl";

	private WMSLogger logger;
	private String names = "*";
	private Pattern namesRegex = null;
	private boolean matchAllow = true;
	private boolean noMatchAllow = false;

	public void onAppStart(IApplicationInstance appInstance)
	{
		logger = WMSLoggerFactory.getLoggerObj(appInstance);

		// Compile the regex if it is set
		String namesRegexStr = appInstance.getProperties().getPropertyStr(PROP_NAME_PREFIX + "NamesRegex");
		if (namesRegexStr != null && namesRegexStr.length() > 0) {
			namesRegex = Pattern.compile(namesRegexStr);
		}
		
		names = appInstance.getProperties().getPropertyStr(PROP_NAME_PREFIX + "Names", names);
		matchAllow = appInstance.getProperties().getPropertyBoolean(PROP_NAME_PREFIX + "MatchAllow", matchAllow);
		noMatchAllow = appInstance.getProperties().getPropertyBoolean(PROP_NAME_PREFIX + "NoMatchAllow", noMatchAllow);

		appInstance.setLiveStreamTranscoderControl(new Controller());
	}
}
