package com.zlrab.plugin.extension;


import com.google.gson.Gson;
import com.zlrab.core.UnstableApi;
import com.zlrab.tool.LogTool;
import com.zlrab.tool.RandomTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zlrab
 * @date 2020/12/28 15:26
 */
public class ConfuseExtension {
    public static final String CONF_CONFUSE_NAME = "confuseConf";
    /**
     * 字符串加密开关
     */
    boolean autoEncryptionString;
    /**
     * aop注入开关
     */
    boolean autoJavassistInject;
    /**
     * 混淆组件名开关
     */
    boolean confuseComponent;
    /**
     * 混淆自定义View开关
     */
    boolean confuseCustomView;
    /**
     * 混淆资源名开关
     */
    boolean confuseResName;

    boolean confuseNativeString;
    /**
     * 混淆加密类开关
     * 配置无效 使用{@link ConfuseExtension.Conf.ConfuseClassRulesEntity}配置是否混淆加密类
     */
    @Deprecated
    boolean confuseEncryptedStringClass;
    /**
     * 是否删除class中的.source标记
     */
    boolean removeSource;
    /**
     * 混淆配置路径
     */
    String confuseJsonPath;
    /**
     * 混淆mapping日志输出路径
     * 不指定时默认输出路径：build/ZLRab/mappings/confuseConf_mapping.txt
     */
    String confuseMappingOutPath;
    String debugLogOutPath;
    /**
     * 与confuseJsonPath指向的json中encryptedString.encryptedStringToolJarPath节点功能相同， 为了解决json中写死路径导致的多平台不支持问题，
     * 优先使用此处的配置，当此处的配置为空或指向的路径不存在时，才会去读取json中指向的路径，当json中指向的路径为空或不存在时，将抛出异常
     */
    String encryptedStringToolJarPath;

    String soName;

    private Conf conf;

    private Gson gson = new Gson();

    public String getConfuseJsonPath() {
        return confuseJsonPath;
    }

    public ConfuseExtension setConfuseJsonPath(String confuseJsonPath) {
        this.confuseJsonPath = confuseJsonPath;
        return this;
    }

    public String getConfuseMappingOutPath() {
        return confuseMappingOutPath;
    }

    public ConfuseExtension setConfuseMappingOutPath(String confuseMappingOutPath) {
        this.confuseMappingOutPath = confuseMappingOutPath;
        return this;
    }

    public String getDebugLogOutPath() {
        return debugLogOutPath;
    }

    public ConfuseExtension setDebugLogOutPath(String debugLogOutPath) {
        this.debugLogOutPath = debugLogOutPath;
        return this;
    }

    public boolean isConfuseComponent() {
        return confuseComponent;
    }

    public ConfuseExtension setConfuseComponent(boolean confuseComponent) {
        this.confuseComponent = confuseComponent;
        return this;
    }

    public boolean isRemoveSource() {
        return removeSource;
    }

    public ConfuseExtension setRemoveSource(boolean removeSource) {
        this.removeSource = removeSource;
        return this;
    }

    public boolean isConfuseCustomView() {
        return confuseCustomView;
    }

    public ConfuseExtension setConfuseCustomView(boolean confuseCustomView) {
        this.confuseCustomView = confuseCustomView;
        return this;
    }

    public String getEncryptedStringToolJarPath() {
        return encryptedStringToolJarPath;
    }

    public ConfuseExtension setEncryptedStringToolJarPath(String encryptedStringToolJarPath) {
        this.encryptedStringToolJarPath = encryptedStringToolJarPath;
        return this;
    }

    public boolean isAutoEncryptionString() {
        return autoEncryptionString;
    }

    public ConfuseExtension setAutoEncryptionString(boolean autoEncryptionString) {
        this.autoEncryptionString = autoEncryptionString;
        return this;
    }

    public boolean isAutoJavassistInject() {
        return autoJavassistInject;
    }

    public ConfuseExtension setAutoJavassistInject(boolean autoJavassistInject) {
        this.autoJavassistInject = autoJavassistInject;
        return this;
    }

    public String getSoName() {
        return soName;
    }

    public void setSoName(String soName) {
        this.soName = soName;
    }

    @Deprecated
    public boolean isConfuseEncryptedStringClass() {
        return confuseEncryptedStringClass;
    }

    @Deprecated
    public ConfuseExtension setConfuseEncryptedStringClass(boolean confuseEncryptedStringClass) {
        this.confuseEncryptedStringClass = confuseEncryptedStringClass;
        return this;
    }

    public boolean isConfuseResName() {
        return confuseResName;
    }

    public ConfuseExtension setConfuseResName(boolean confuseResName) {
        this.confuseResName = confuseResName;
        return this;
    }

    public boolean isConfuseNativeString() {
        return confuseNativeString;
    }

    public void setConfuseNativeString(boolean confuseNativeString) {
        this.confuseNativeString = confuseNativeString;
    }

    public Conf getConf() {
        if (conf == null) {
            try {
                conf = gson.fromJson(new FileReader(new File(confuseJsonPath)), Conf.class);
                supplement(conf.methodNameDictionary);
                supplement(conf.packageClassDictionary);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                LogTool.w("解析confuseJsonPath失败，将使用随机生成的混淆规则 , confuseJsonPath = \"" + confuseJsonPath);
                conf = new Conf();
                List<String> methodNameDictionary = new ArrayList<>();
                List<String> packageClassDictionary = new ArrayList<>();
                for (int index = 0; index < Conf.DICTIONARY_SIZE; index++) {
                    methodNameDictionary.add(RandomTool.randomName());
                    packageClassDictionary.add(RandomTool.randomName());
                }
                Conf.MatchOperationRulesEntity matchOperationRulesEntity = new Conf.MatchOperationRulesEntity();
                matchOperationRulesEntity.component = new ArrayList<>();
                matchOperationRulesEntity.customView = new ArrayList<>();

                Conf.WhitelistEntity whitelistEntity = new Conf.WhitelistEntity();
                whitelistEntity.component = new ArrayList<>();
                whitelistEntity.customView = new ArrayList<>();

                conf.setMethodNameDictionary(methodNameDictionary);
                conf.setPackageClassDictionary(packageClassDictionary);
                conf.setMatchOperationRules(matchOperationRulesEntity);
                conf.setWhitelist(whitelistEntity);
            }
        }
        return conf;
    }

    private void supplement(List<String> dictionary) {
        for (int index = 0; index < Conf.DICTIONARY_SIZE - dictionary.size(); index++) {
            dictionary.add(RandomTool.randomName());
        }
    }

    /**
     * from HookPlugin\src\main\resources\ConfuseConf.json
     */
    public static class Conf {
        /**
         * 字典最大个数
         */
        public static int DICTIONARY_SIZE = 26;
        /**
         * 方法名的混淆字典
         */
        private List<String> methodNameDictionary;
        /**
         * 混淆匹配规则配置
         */
        private MatchOperationRulesEntity MatchOperationRules;
        /**
         * 加密配置
         */
        private EncryptedStringEntity encryptedString;
        /**
         * 混淆白名单配置
         */
        private WhitelistEntity whitelist;
        /**
         * 类名的混淆字典
         */
        private List<String> packageClassDictionary;
        /**
         * 需要删除的类的匹配规则配置
         */
        private RemoveClassRulesEntity removeClassRules;
        /**
         * 需要混淆的类的匹配规则配置
         */
        private ConfuseClassRulesEntity confuseClassRules;
        /**
         * 需要混淆的资源的匹配规则配置
         */
        private ConfuseResNameEntity confuseResName;

        public void setConfuseResName(ConfuseResNameEntity confuseResName) {
            this.confuseResName = confuseResName;
        }

        public ConfuseResNameEntity getConfuseResName() {
            return confuseResName;
        }

        public List<String> getMethodNameDictionary() {
            return methodNameDictionary;
        }

        public Conf setMethodNameDictionary(List<String> methodNameDictionary) {
            this.methodNameDictionary = methodNameDictionary;
            return this;
        }

        public MatchOperationRulesEntity getMatchOperationRules() {
            return MatchOperationRules;
        }

        public Conf setMatchOperationRules(MatchOperationRulesEntity matchOperationRules) {
            MatchOperationRules = matchOperationRules;
            return this;
        }

        public EncryptedStringEntity getEncryptedString() {
            return encryptedString;
        }

        public Conf setEncryptedString(EncryptedStringEntity encryptedString) {
            this.encryptedString = encryptedString;
            return this;
        }

        public WhitelistEntity getWhitelist() {
            return whitelist;
        }

        public Conf setWhitelist(WhitelistEntity whitelist) {
            this.whitelist = whitelist;
            return this;
        }

        public List<String> getPackageClassDictionary() {
            return packageClassDictionary;
        }

        public Conf setPackageClassDictionary(List<String> packageClassDictionary) {
            this.packageClassDictionary = packageClassDictionary;
            return this;
        }

        public RemoveClassRulesEntity getRemoveClassRules() {
            return removeClassRules;
        }

        public Conf setRemoveClassRules(RemoveClassRulesEntity removeClassRules) {
            this.removeClassRules = removeClassRules;
            return this;
        }

        public ConfuseClassRulesEntity getConfuseClassRules() {
            return confuseClassRules;
        }

        public Conf setConfuseClassRules(ConfuseClassRulesEntity confuseClassRules) {
            this.confuseClassRules = confuseClassRules;
            return this;
        }

        public static class ConfuseResNameEntity {
            /**
             * 统一前缀
             */
            private String prefix;
            /**
             * 匹配规则
             */
            private List<String> match;
            /**
             * 白名单
             */
            private List<String> whitelist;

            public void setPrefix(String prefix) {
                this.prefix = prefix;
            }

            public void setMatch(List<String> match) {
                this.match = match;
            }

            public void setWhitelist(List<String> whitelist) {
                this.whitelist = whitelist;
            }

            public String getPrefix() {
                return prefix;
            }

            public List<String> getMatch() {
                return match;
            }

            public List<String> getWhitelist() {
                return whitelist;
            }
        }

        public static class MatchOperationRulesEntity {
            /**
             * 四大组件的匹配规则
             * 当扫描到四大组件时，将与此字典进行匹配，通过后将进行后续操作
             */
            private List<String> component;
            /**
             * 自定义view的混淆匹配规则
             * 当扫描到自定义view时，将与此字典进行匹配，通过后将进行后续操作
             */
            private List<String> customView;

            public void setComponent(List<String> component) {
                this.component = component;
            }

            public void setCustomView(List<String> customView) {
                this.customView = customView;
            }

            public List<String> getComponent() {
                return component;
            }

            public List<String> getCustomView() {
                return customView;
            }
        }

        public static class EncryptedStringEntity {
            /**
             * 加解密jar路径
             */
            private String encryptedStringToolJarPath;
            /**
             * 加解密的class路径，加解密函数名等配置
             */
            private List<EncryptionToolClassEntity> encryptionToolClass;

            public void setEncryptedStringToolJarPath(String encryptedStringToolJarPath) {
                this.encryptedStringToolJarPath = encryptedStringToolJarPath;
            }

            public void setEncryptionToolClass(List<EncryptionToolClassEntity> encryptionToolClass) {
                this.encryptionToolClass = encryptionToolClass;
            }

            public String getEncryptedStringToolJarPath() {
                return encryptedStringToolJarPath;
            }

            public List<EncryptionToolClassEntity> getEncryptionToolClass() {
                return encryptionToolClass;
            }

            public static class EncryptionToolClassEntity {
                /**
                 * 用于加密的Class的路径
                 * example : com.zlrab.Encode
                 */
                private String encodeClassName;
                /**
                 * 用于解密的Class的路径
                 * example : com.zlrab.Decode
                 */
                private String decodeClassName;
                /**
                 * 是否在完成加密注入后自动删除解密函数
                 * TODO
                 */
                private String autoRemoveEncodeMethod;
                /**
                 * 解密函数名
                 */
                private String decodeMethodName;
                /**
                 * 加密函数
                 */
                private String encodeMethodName;
                /**
                 * 加解密key 为空时使用随机生成的key
                 */
                private String key;
                /**
                 * 特殊支持 指示加密类的byte数组类型的名字 在混淆过程中将修改对应的byte数组
                 */
                @UnstableApi
                private String byteArrayKeyName;

                public void setEncodeClassName(String encodeClassName) {
                    this.encodeClassName = encodeClassName;
                }

                public void setDecodeClassName(String decodeClassName) {
                    this.decodeClassName = decodeClassName;
                }

                public void setAutoRemoveEncodeMethod(String autoRemoveEncodeMethod) {
                    this.autoRemoveEncodeMethod = autoRemoveEncodeMethod;
                }

                public void setDecodeMethodName(String decodeMethodName) {
                    this.decodeMethodName = decodeMethodName;
                }

                public void setEncodeMethodName(String encodeMethodName) {
                    this.encodeMethodName = encodeMethodName;
                }

                public void setKey(String key) {
                    this.key = key;
                }

                public String getEncodeClassName() {
                    return encodeClassName;
                }

                public String getDecodeClassName() {
                    return decodeClassName;
                }

                public String getAutoRemoveEncodeMethod() {
                    return autoRemoveEncodeMethod;
                }

                public String getDecodeMethodName() {
                    return decodeMethodName;
                }

                public String getEncodeMethodName() {
                    return encodeMethodName;
                }

                public String getKey() {
                    return key;
                }

                public String getByteArrayKeyName() {
                    return byteArrayKeyName;
                }

                public EncryptionToolClassEntity setByteArrayKeyName(String byteArrayKeyName) {
                    this.byteArrayKeyName = byteArrayKeyName;
                    return this;
                }
            }
        }

        public static class WhitelistEntity {
            /**
             * 四大组件的白名单匹配规则
             * 当扫描到四大组件时，将与此字典进行匹配，通过后将排除这个组件，不做操作
             */
            private List<String> component;
            /**
             * 自定义view的白名单匹配规则
             * 当扫描到自定义view时，将与此字典进行匹配，通过后将排除这个Class，不做操作
             */
            private List<String> customView;

            public void setComponent(List<String> component) {
                this.component = component;
            }

            public void setCustomView(List<String> customView) {
                this.customView = customView;
            }

            public List<String> getComponent() {
                return component;
            }

            public List<String> getCustomView() {
                return customView;
            }
        }

        public static class RemoveClassRulesEntity {
            /**
             * match : ["android.util.Base64"]
             * whitelist : ["com/zlrab/Demo"]
             */
            private List<String> match;
            private List<String> whitelist;

            public void setMatch(List<String> match) {
                this.match = match;
            }

            public void setWhitelist(List<String> whitelist) {
                this.whitelist = whitelist;
            }

            public List<String> getMatch() {
                return match;
            }

            public List<String> getWhitelist() {
                return whitelist;
            }
        }

        public static class ConfuseClassRulesEntity {
            /**
             * 需要混淆的类的匹配规则配置容器
             */
            private List<MatchEntity> match;
            /**
             * 白名单，当class与match匹配时，可通过白名单决定不做操作
             * com/zlrab/demo
             */
            private List<String> whitelist;

            public void setMatch(List<MatchEntity> match) {
                this.match = match;
            }

            public void setWhitelist(List<String> whitelist) {
                this.whitelist = whitelist;
            }

            public List<MatchEntity> getMatch() {
                return match;
            }

            public List<String> getWhitelist() {
                return whitelist;
            }

            public static class MatchEntity {
                /**
                 * 需要混淆的方法匹配规则配置容器
                 */
                private List<MethodsEntity> methods;
                /**
                 * 需要混淆的类
                 * 支持通配符
                 * example : com/zlrab/*
                 */
                private String classNameRules;

                public void setMethods(List<MethodsEntity> methods) {
                    this.methods = methods;
                }

                public void setClassNameRules(String classNameRules) {
                    this.classNameRules = classNameRules;
                }

                public List<MethodsEntity> getMethods() {
                    return methods;
                }

                public String getClassNameRules() {
                    return classNameRules;
                }

                public class MethodsEntity {
                    /**
                     * 方法的访问权限{@link org.objectweb.asm.Opcodes}
                     */
                    private int access;
                    /**
                     * 方法的名字
                     */
                    private String name;
                    /**
                     * 操作当前方法的动作:
                     * 支持两种操作 : remove , confuse
                     */
                    private String action;
                    /**
                     *
                     */
                    private String descriptor;

                    public void setAccess(int access) {
                        this.access = access;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public void setAction(String action) {
                        this.action = action;
                    }

                    public void setDescriptor(String descriptor) {
                        this.descriptor = descriptor;
                    }

                    public int getAccess() {
                        return access;
                    }

                    public String getName() {
                        return name;
                    }

                    public String getAction() {
                        return action;
                    }

                    public String getDescriptor() {
                        return descriptor;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ConfuseExtension{" +
                "autoEncryptionString=" + autoEncryptionString +
                ", autoJavassistInject=" + autoJavassistInject +
                ", confuseComponent=" + confuseComponent +
                ", confuseCustomView=" + confuseCustomView +
                ", removeSource=" + removeSource +
                ", confuseJsonPath='" + confuseJsonPath + '\'' +
                ", confuseMappingOutPath='" + confuseMappingOutPath + '\'' +
                ", debugLogOutPath='" + debugLogOutPath + '\'' +
                ", encryptedStringToolJarPath='" + encryptedStringToolJarPath + '\'' +
                '}';
    }
}
