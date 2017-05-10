package io.github.shadowchild.cybernize.util;


import io.github.shadowchild.cybernize.newconfig.Config;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Zach Piddock on 31/12/2015.
 */
public class EnumHelper {

    private static Object reflectionFactory = null;
    private static Method newConstructorAccessor = null;
    private static Method newInstance = null;
    private static Method newFieldAccessor = null;
    private static Method fieldAccessorSet = null;
    private static boolean isSetup = false;

    //Some enums are decompiled with extra arguments, so lets check for that
    @SuppressWarnings("rawtypes") private static Class[][] commonTypes = { { Config.Type.class, Class.class } };

    private static void setup() {

        if(isSetup) {
            return;
        }

        try {
            Method getReflectionFactory = Class.forName("sun.reflect.ReflectionFactory")
                    .getDeclaredMethod("getReflectionFactory");
            reflectionFactory = getReflectionFactory.invoke(null);
            newConstructorAccessor = Class.forName("sun.reflect.ReflectionFactory")
                    .getDeclaredMethod("newConstructorAccessor", Constructor.class);
            newInstance = Class.forName("sun.reflect.ConstructorAccessor")
                    .getDeclaredMethod("newInstance", Object[].class);
            newFieldAccessor = Class.forName("sun.reflect.ReflectionFactory")
                    .getDeclaredMethod("newFieldAccessor", Field.class, boolean.class);
            fieldAccessorSet = Class.forName("sun.reflect.FieldAccessor")
                    .getDeclaredMethod("set", Object.class, Object.class);
        } catch(Exception e) {
            e.printStackTrace();
        }

        isSetup = true;
    }

    /*
     * Everything below this is found at the site below, and updated to be able to compile in Eclipse/Java 1.6+
     * Also modified for use in decompiled code.
     * Found at: http://niceideas.ch/roller2/badtrash/entry/java_create_enum_instances_dynamically
     */
    private static Object getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes) throws
            Exception {

        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2,
                additionalParameterTypes.length
        );
        return newConstructorAccessor.invoke(reflectionFactory,
                enumClass.getDeclaredConstructor(parameterTypes)
        );
    }

    private static <T extends Enum<?>> T makeEnum(Class<T> enumClass, String value, int ordinal, Class<?>[] additionalTypes, Object[] additionalValues) throws
            Exception {

        Object[] parms = new Object[additionalValues.length + 2];
        parms[0] = value;
        parms[1] = Integer.valueOf(ordinal);
        System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
        return enumClass.cast(newInstance.invoke(getConstructorAccessor(enumClass, additionalTypes),
                new Object[] { parms }
        ));
    }

    public static void setFailsafeFieldValue(Field field, Object target, Object value) throws
            Exception {

        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        Object fieldAccessor = newFieldAccessor.invoke(reflectionFactory, field, false);
        fieldAccessorSet.invoke(fieldAccessor, target, value);
    }

    private static void blankField(Class<?> enumClass, String fieldName) throws Exception {

        for(Field field : Class.class.getDeclaredFields()) {
            if(field.getName().contains(fieldName)) {
                field.setAccessible(true);
                setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }

    private static void cleanEnumCache(Class<?> enumClass) throws Exception {

        blankField(enumClass, "enumConstantDirectory");
        blankField(enumClass, "enumConstants");
    }

    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName, Object... paramValues) {

        setup();
        return addEnum(commonTypes, enumType, enumName, paramValues);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends Enum<?>> T addEnum(Class[][] map, Class<T> enumType, String enumName, Object... paramValues) {

        for(Class[] lookup : map) {
            if(lookup[0] == enumType) {
                Class<?>[] paramTypes = new Class<?>[lookup.length - 1];
                if(paramTypes.length > 0) {
                    System.arraycopy(lookup, 1, paramTypes, 0, paramTypes.length);
                }
                return addEnum(enumType, enumName, paramTypes, paramValues);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName, Class<?>[] paramTypes, Object[] paramValues) {

        if(!isSetup) {
            setup();
        }

        Field valuesField = null;
        Field[] fields = enumType.getDeclaredFields();

        for(Field field : fields) {
            String name = field.getName();
            if(name.equals("$VALUES") || name.equals(
                    "ENUM$VALUES")) //Added 'ENUM$VALUES' because Eclipse's internal compiler doesn't follow standards
            {
                valuesField = field;
                break;
            }
        }

        int flags = (Utils.BUILT ? Modifier.PUBLIC : Modifier.PRIVATE) | Modifier.STATIC | Modifier.FINAL | 0x1000 /*SYNTHETIC*/;
        if(valuesField == null) {
            String valueType = String.format("[L%s;", enumType.getName().replace('.', '/'));

            for(Field field : fields) {
                if((field.getModifiers() & flags) == flags && field.getType()
                        .getName()
                        .replace('.', '/')
                        .equals(valueType)) //Apparently some JVMs return .'s and some don't..
                {
                    valuesField = field;
                    break;
                }
            }
        }

        if(valuesField == null) {
            //            FMLLog.severe("Could not find $VALUES field for enum: %s", enumType.getName());
            //            FMLLog.severe("Runtime Deobf: %s", FMLForgePlugin.RUNTIME_DEOBF);
            //            FMLLog.severe("Flags: %s", String.format("%16s", Integer.toBinaryString(flags)).replace(' ', '0'));
            //            FMLLog.severe("Fields:");
            //            for (Field field : fields)
            //            {
            //                String mods = String.format("%16s", Integer.toBinaryString(field.getModifiers())).replace(' ', '0');
            //                FMLLog.severe("       %s %s: %s", mods, field.getName(), field.getType().getName());
            //            }
            return null;
        }

        valuesField.setAccessible(true);

        try {
            T[] previousValues = (T[])valuesField.get(enumType);
            List<T> values = new ArrayList<T>(Arrays.asList(previousValues));
            T newValue = (T)makeEnum(enumType, enumName, values.size(), paramTypes, paramValues);
            values.add(newValue);
            setFailsafeFieldValue(valuesField, null,
                    values.toArray((T[])Array.newInstance(enumType, 0))
            );
            cleanEnumCache(enumType);

            return newValue;
        } catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static {
        if(!isSetup) {
            setup();
        }
    }
}
