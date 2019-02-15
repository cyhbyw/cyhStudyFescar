/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.fescar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fescar.common.exception.NotSupportYetException;
import com.alibaba.nacos.api.exception.NacosException;

/**
 * The type Configuration factory.
 *
 * @Author: jimin.jm @alibaba-inc.com
 * @Project: fescar -all
 * @DateTime: 2018 /12/24 10:54
 * @FileName: ConfigurationFactory
 * @Description:
 */
public final class ConfigurationFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationFactory.class);
    private static final String REGISTRY_CONF = "registry.conf";
    public static final Configuration FILE_INSTANCE = new FileConfiguration(REGISTRY_CONF);
    public static final String FILE_ROOT_REGISTRY = "registry";
    public static final String FILE_ROOT_TYPE = "type";
    public static final String FILE_CONFIG_SPLIT_CHAR = ".";
    private static final String NAME_KEY = "name";
    public static final String FILE_TYPE = "file";

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static Configuration getInstance() {
        ConfigType configType = null;
        try {
            configType = ConfigType
                    .getType(FILE_INSTANCE.getConfig(FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + FILE_ROOT_TYPE));
        } catch (Exception exx) {
            LOGGER.error(exx.getMessage());
        }
        Configuration configuration;
        switch (configType) {
            case Nacos:
                try {
                    configuration = new NacosConfiguration();
                } catch (NacosException e) {
                    throw new RuntimeException(e);
                }
                break;
            case File:
                String pathDataId =
                        FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + FILE_TYPE + FILE_CONFIG_SPLIT_CHAR + NAME_KEY;
                String name = FILE_INSTANCE.getConfig(pathDataId);
                configuration = new FileConfiguration(name);
                break;
            default:
                throw new NotSupportYetException("not support register type:" + configType);
        }
        return configuration;
    }
}