package de.Zorro909.ConfigurationLibrary;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.internal.Primitives;

import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObject;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializers;

public abstract class ConfigurationAbstraction implements Configuration {

	public void set(String path, Object value) {
		if (value instanceof String || Primitives.isPrimitive(value.getClass())
				|| Primitives.isWrapperType(value.getClass())) {
			setInternal(path, String.valueOf(value));
		} else {
			Serializer serializer = Serializers.getSerializer(value.getClass());
			if (serializer == null) {
				System.err.println("[Configuration] Can't find Serializer for Type " + value.getClass().getName());
				return;
			}
			setSerializedObject(path, serializer.serialize(value));
		}
	}

	public void setList(String path, List list) {
		if (list.size() == 0 || list.get(0).getClass().isPrimitive()) {
			setInternalList(path, list);
		} else {
			Serializer serializer = Serializers.getSerializer(list.get(0).getClass());
			if (serializer == null) {
				System.err
						.println("[Configuration] Can't find Serializer for Type " + list.get(0).getClass().getName());
				return;
			}
			markAsList(path);
			setSerializedObjectList(path, list);
		}
	}

	public <T> T get(String path, Class<T> type) {
		if (type == String.class || Primitives.isPrimitive(type) || Primitives.isWrapperType(type)) {
			Object value = get(path);
			if (value == null) {
				return null;
			}
			if (value.getClass().equals(type)) {
				return (T) value;
			} else {
				if (value.equals("null")) {
					return null;
				}
				return (T) convertToPrimitive(String.valueOf(value), Primitives.wrap(type));
			}
		} else {
			Serializer<T> serializer = Serializers.getSerializer(type);
			if (serializer == null) {
				System.err.println("[Configuration] Can't find De/Serializer for Type " + type.getName());
				return null;
			}
			SerializedObject sObject = get(path, serializer.getObjectStructure());
			try {
				if (sObject == null) {
					return null;
				} else {
					return serializer.deserialize(sObject);
				}
			} catch (Exception e) {
				throw new RuntimeException(
						"[Configuration] Can't deserialize Type " + type.getName() + " from path: " + path, e);
			}
		}
	}

	public <T> List<T> getList(String path, Class<T> type) {
		List<T> list = new ArrayList<T>();
		List<Object> source = getList(path);

		if (source == null) {
			return null;
		}

		if (type == String.class || Primitives.isPrimitive(type) || Primitives.isWrapperType(type)) {
			for (Object value : source) {
				if (value.getClass().equals(type)) {
					list.add((T) value);
				} else {
					list.add((T) convertToPrimitive(String.valueOf(value), Primitives.wrap(type)));
				}
			}
			return list;
		} else {
			Serializer<T> serializer = Serializers.getSerializer(type);
			if (serializer == null) {
				System.err.println("[Configuration] Can't find De/Serializer for Type " + type.getName());
				return null;
			}
			List<SerializedObject> sObjs = getList(path, serializer.getObjectStructure());
			for (int i = 0; i < source.size(); i++) {
				try {
					list.add(serializer.deserialize(sObjs.get(0)));
				} catch (Exception e) {
					throw new RuntimeException("[Configuration] Can't deserialize Type " + type.getName()
							+ " from path: " + path + "." + i, e);
				}
			}
			return list;
		}
	}

	private Object convertToPrimitive(String value, Class type) {
		try {
			if (type == String.class) {
				return value;
			} else if (type == Integer.class) {
				return Integer.valueOf(value);
			} else if (type == Double.class) {
				return Double.valueOf(value);
			} else if (type == Float.class) {
				return Float.valueOf(value);
			} else if (type == Boolean.class) {
				return Boolean.valueOf(value);
			} else if (type == Short.class) {
				return Short.valueOf(value);
			} else if (type == Character.class) {
				return value.charAt(0);
			} else if (type == Byte.class) {
				return value.getBytes()[0];
			} else if (type == Long.class) {
				return Long.valueOf(value);
			}
		} catch (Exception e) {
			throw new RuntimeException("Could not convert \"" + value + "\" to primitive of type " + type.getName(), e);
		}
		return null;
	}

	public ConfigurationPane getRegion(String prefix) {
		return new ConfigurationPane(this, prefix);
	}

	public String getString(String path) {
		return String.valueOf(get(path));
	}

	public abstract void setInternal(String path, Object primitiveValue);

	public abstract void setInternalList(String path, List primitiveList);

}
