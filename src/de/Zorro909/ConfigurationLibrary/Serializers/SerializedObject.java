package de.Zorro909.ConfigurationLibrary.Serializers;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.internal.Primitives;

public class SerializedObject {
	private HashMap<String, Object> serializedMap = new HashMap<>();

	private SerializedObjectStructure objectStructure;

	public SerializedObject(SerializedObjectStructure objectStructure) {
		this.objectStructure = objectStructure;
	}

	public void set(String path, Object value) {
		if (value == null)
			return;
		if (objectStructure.getType(path) != value.getClass()) {
			throw new RuntimeException("Malfunction of Serializer for Type '"
					+ objectStructure.getUnderlyingClass().getName() + "' due to incorrectly set Value. (Expected: "
					+ objectStructure.getType(path) + ", Actual: " + value.getClass().getName() + ")");
		}
		if (value instanceof String || Primitives.isPrimitive(value.getClass())
				|| Primitives.isWrapperType(value.getClass())) {
			serializedMap.put(path, value);
		} else {
			Serializer serializer = Serializers.getSerializer(value.getClass());
			if (serializer == null) {
				throw new RuntimeException("No Serializer found for Type '" + value.getClass().getName() + "'!");
			}
			serializedMap.put(path, value);
		}
	}

	public <T> void setList(String path, List<T> value, Class<T> type) {
		if (value == null)
			return;
		if (!objectStructure.isList(path) || ((objectStructure.isObject(path)
				? ((SerializedObject) objectStructure.getType(path)).getUnderlyingClass() != type.getClass()
				: objectStructure.getType(path) != type))) {
			throw new RuntimeException("Malfunction of Serializer for Type '"
					+ objectStructure.getUnderlyingClass().getName() + "' due to incorrectly set Value. (Expected: "
					+ objectStructure.getType(path) + ", Actual: " + value.getClass().getName() + ")");
		}
		if (type == String.class || Primitives.isPrimitive(type) || Primitives.isWrapperType(type)) {
			serializedMap.put(path, value);
		} else {
			Serializer<T> serializer = Serializers.getSerializer(type);
			if (serializer == null) {
				throw new RuntimeException("No Serializer found for Type '" + value.getClass().getName() + "'!");
			}
			serializedMap.put(path,
					value.stream().map((obj) -> serializer.serialize(obj)).collect(Collectors.toList()));
		}
	}

	public void setSerializedObject(String path, SerializedObject sObj) {
		if (objectStructure.getType(path) != sObj.getStructure()) {
			throw new RuntimeException("Malfunction of Serializer for Type '"
					+ objectStructure.getUnderlyingClass().getName() + "' due to incorrectly set Value. (Expected: "
					+ objectStructure.getType(path) + ", Actual: " + sObj.getUnderlyingClass().getName() + ")");
		}
		Serializer serializer = Serializers.getSerializer(sObj.getUnderlyingClass());
		if (serializer == null) {
			throw new RuntimeException("No Serializer found for Type '" + sObj.getUnderlyingClass().getName() + "'!");
		}
		serializedMap.put(path, serializer.deserialize(sObj));
	}

	public <T> T get(String path) {
		return (T) serializedMap.get(path);
	}

	public <T> T get(String path, T def) {
		if (serializedMap.containsKey(path)) {
			return get(path);
		}
		return def;
	}

	public Class<?> getUnderlyingClass() {
		return objectStructure.getUnderlyingClass();
	}

	public HashMap<String, Object> getSerializedMap() {
		return serializedMap;
	}

	public SerializedObjectStructure getStructure() {
		return objectStructure;
	}
}
