package de.Zorro909.ConfigurationLibrary.Implementations;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.Zorro909.ConfigurationLibrary.Config;
import de.Zorro909.ConfigurationLibrary.Configuration;
import de.Zorro909.ConfigurationLibrary.ConfigurationAbstraction;
import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObject;
import de.Zorro909.ConfigurationLibrary.Serializers.SerializedObjectStructure;

public class JsonConfiguration extends ConfigurationAbstraction {

	private HashMap<String, Object> map = new HashMap<String, Object>();
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private String identifier;
	private File confFile;

	public JsonConfiguration(Config configAnnotation, Configuration unusedPreviousConfiguration) {
		this(configAnnotation.value());
	}

	public JsonConfiguration(String value) {
		identifier = value;
		if (value.startsWith("http")) {
			JsonObject config = null;
			try {
				config = gson.fromJson(new InputStreamReader(new URL(value).openStream()), JsonObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loadConfig(map, config);
			return;
		}
		confFile = new File(value);
		if (!confFile.exists()) {
			if (!confFile.getParentFile().exists()) {
				confFile.getParentFile().mkdirs();
			}
		} else {
			JsonObject config = null;
			try {
				config = gson.fromJson(new FileReader(confFile), JsonObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			loadConfig(map, config);
		}
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	private void loadConfig(HashMap<String, Object> toAdd, JsonObject config) {
		for (String key : config.keySet()) {
			JsonElement val = config.get(key);
			if (val.isJsonObject()) {
				HashMap<String, Object> mapToAdd = new HashMap<>();
				toAdd.put(key, mapToAdd);
				loadConfig(mapToAdd, val.getAsJsonObject());
			} else if (val.isJsonArray()) {
				List<Object> list = new ArrayList<Object>();
				JsonArray array = val.getAsJsonArray();
				for (int i = 0; i < array.size(); i++) {
					JsonElement entry = array.get(i);
					if (entry.isJsonObject()) {
						HashMap<String, Object> mapToAdd = new HashMap<>();
						list.add(mapToAdd);
						loadConfig(mapToAdd, entry.getAsJsonObject());
					} else {
						list.add(entry.getAsString());
					}
				}
				toAdd.put(key, list);
			} else {
				toAdd.put(key, config.getAsString());
			}
		}
	}

	@Override
	public Object get(String path) {
		Object ret = traverseTree(map, path, false);
		if (ret instanceof Map || ret instanceof List) {
			return getRegion(path);
		}
		return ret;
	}

	private Object traverseTree(Object currentElement, String path, boolean createMissing) {
		if (!(currentElement instanceof Map || currentElement instanceof List)) {
			return null;
		}
		String next = "";
		if (path.contains(".")) {
			String[] split = path.split("\\.", 2);
			next = split[0];
			path = split[1];
		} else {
			next = path;
			path = "";
		}
		if (currentElement instanceof Map) {
			Object nextElement = ((Map) currentElement).get(next);
			if (nextElement == null) {
				if (!createMissing) {
					return null;
				} else {
					nextElement = new HashMap<String, Object>();
					((Map) currentElement).put(next, nextElement);
				}
			}
			if (!path.isEmpty()) {
				return traverseTree(nextElement, path, createMissing);
			}
			return nextElement;
		} else {
			int element = 0;
			try {
				element = Integer.valueOf(next);
			} catch (Exception e) {
				throw new RuntimeException(
						"Encountered a non-number while expecting an Index for a List (Value: " + next + ")");
			}
			List list = (List) currentElement;
			if (list.size() > element) {
				currentElement = list.get(element);
				if (!path.isEmpty()) {
					return traverseTree(currentElement, path, createMissing);
				}
				return currentElement;
			} else if (createMissing) {
				currentElement = new HashMap<String, Object>();
				while (list.size() < element) {
					list.add(null);
				}
				list.add(element, currentElement);
				return currentElement;
			} else {
				return null;
			}
		}
	}

	@Override
	public <T> List<T> getList(String path) {
		Object ret = traverseTree(map, path, false);
		if (ret instanceof List) {
			return (List<T>) ret;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInternal(String path, Object primitiveValue) {
		Object traversedPath = null;
		if (!path.contains(".")) {
			traversedPath = map;
		} else {
			traversedPath = traverseTree(map, path.substring(0, path.lastIndexOf(".")), true);
		}
		if (traversedPath instanceof Map) {
			((Map) traversedPath).put(path.substring(path.lastIndexOf(".") + 1), primitiveValue);
		} else if (traversedPath instanceof List) {
			String childName = path.substring(path.lastIndexOf(".") + 1);
			List list = (List) traversedPath;
			int element = 0;
			try {
				element = Integer.valueOf(childName);
			} catch (Exception e) {
				throw new RuntimeException(
						"Encountered a non-number while expecting an Index for a List (Value: " + childName + ")");
			}
			while (list.size() < element) {
				list.add(null);
			}
			list.add(element, primitiveValue);
		}
	}

	@Override
	public void setInternalList(String path, List primitiveList) {
		Object traversedPath = null;
		if (!path.contains(".")) {
			traversedPath = map;
		} else {
			traversedPath = traverseTree(map, path.substring(0, path.lastIndexOf(".")), true);
		}
		if (traversedPath instanceof Map) {
			((Map) traversedPath).put(path.substring(path.lastIndexOf(".") + 1), primitiveList);
		} else if (traversedPath instanceof List) {
			String childName = path.substring(path.lastIndexOf(".") + 1);
			List list = (List) traversedPath;
			int element = 0;
			try {
				element = Integer.valueOf(childName);
			} catch (Exception e) {
				throw new RuntimeException(
						"Encountered a non-number while expecting an Index for a List (Value: " + childName + ")");
			}
			while (list.size() < element) {
				list.add(null);
			}
			list.add(element, primitiveList);
		}
	}

	@Override
	public void save() {
		JsonObject jo = new JsonObject();
		_save(jo, map);
		try {
			Files.newBufferedWriter(confFile.toPath(), StandardOpenOption.CREATE_NEW).write(gson.toJson(jo));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void _save(JsonObject conf, Map<String, Object> map) {
		for (String key : map.keySet()) {
			Object val = map.get(key);
			if (val instanceof Map) {
				JsonObject obj = new JsonObject();
				conf.add(key, obj);
				_save(obj, (Map<String, Object>) val);
			} else if (val instanceof List) {
				JsonArray sequence = new JsonArray();
				for (Object s : (List) val) {
					if (s instanceof Map) {
						JsonObject obj = new JsonObject();
						sequence.add(obj);
						_save(obj, (Map<String, Object>) s);
					} else {
						sequence.add((String) s);
					}
				}
			} else {
				conf.addProperty(key, (String) val);
			}
		}
	}

	@Override
	public void markAsList(String path) {
		setInternalList(path, new ArrayList<Object>());
	}

	@Override
	public List<String> getKeys() {
		return Arrays.asList(map.keySet().toArray(new String[] {}));
	}

	@Override
	public List<String> getKeys(String path) {
		Object ret = traverseTree(map, path, false);
		if (ret == null || !(ret instanceof Map)) {
			return new ArrayList<>();
		} else {
			return Arrays.asList(((HashMap<String, Object>) ret).keySet().toArray(new String[] {}));
		}
	}

	@Override
	public Object get(String path, Object def) {
		Object ret = get(path);
		if (ret == null) {
			return def;
		}
		return ret;
	}

	@Override
	public <T> List<T> getList(String path, List<T> def) {
		List<T> ret = getList(path);
		if (ret == null) {
			return def;
		}
		return ret;
	}

	@Override
	public <T> List<T> getList(String path, Class<T> type, List<T> def) {
		List<T> ret = getList(path);
		if (ret == null) {
			return def;
		}
		return ret;
	}

	@Override
	public String getString(String path, String def) {
		String ret = getString(path);
		if (ret == null) {
			return def;
		}
		return ret;
	}

	@Override
	public boolean exists(String path) {
		return traverseTree(map, path, false) != null;
	}

	@Override
	public void setSerializedObject(String path, SerializedObject sObj) {
		HashMap<String, Object> sMap = sObj.getSerializedMap();
		SerializedObjectStructure sObjStructure = sObj.getStructure();
		for (String key : sMap.keySet()) {
			Class<?> type = (Class<?>) sObjStructure.getType(key);
			boolean list = sObjStructure.isList(key);
			Object value = sMap.get(key);
			if (value == null)
				continue;
			if (value instanceof SerializedObject) {
				setSerializedObject(path + "." + key, (SerializedObject) value);
			} else if (value instanceof List) {
				if (((List) value).size() == 0) {
					markAsList(path + "." + key);
					continue;
				} else {
					if (((List) value).get(0) instanceof SerializedObject) {
						setSerializedObjectList(path + "." + key, (List<SerializedObject>) value);
					} else {
						setList(path + "." + key, (List) value);
					}
				}
			} else {
				set(path + "." + key, value);
			}
		}
	}

	@Override
	public void setSerializedObjectList(String path, List<SerializedObject> sObjects) {
		markAsList(path);
		for (int i = 0; i < sObjects.size(); i++) {
			setSerializedObject(path + "." + i, sObjects.get(i));
		}
	}

	@Override
	public List<SerializedObject> getList(String path, SerializedObjectStructure sObjectStructure) {
		int objectCount = getList(path).size();
		List<SerializedObject> out = new ArrayList<SerializedObject>();
		for (int i = 0; i < objectCount; i++) {
			SerializedObject so = new SerializedObject(sObjectStructure);
			HashMap<String, Object> types = sObjectStructure.getSerializedMap();
			for (String key : types.keySet()) {
				Object type = types.get(key);
				if (sObjectStructure.isList(key)) {
					if (type instanceof SerializedObjectStructure) {
						so.setList(key, getList(path + "." + i + "." + key),
								((SerializedObjectStructure) type).getUnderlyingClass());
					} else {
						so.setList(key, getList(path + "." + i + "." + key), (Class<?>) type);
					}
				} else {
					if (type instanceof SerializedObjectStructure) {
						so.setSerializedObject(key, get(key, (SerializedObjectStructure) type));
					} else {
						so.set(key, get(key));
					}
				}
			}
			out.add(so);
		}
		return out;
	}

	@Override
	public SerializedObject get(String path, SerializedObjectStructure sObjectStructure) {
		SerializedObject so = new SerializedObject(sObjectStructure);
		HashMap<String, Object> types = sObjectStructure.getSerializedMap();
		for (String key : types.keySet()) {
			Object type = types.get(key);
			if (sObjectStructure.isList(key)) {
				if (type instanceof SerializedObjectStructure) {
					so.setList(key, getList(path + "." + key), ((SerializedObjectStructure) type).getUnderlyingClass());
				} else {
					so.setList(key, getList(path + "." + key), (Class<?>) type);
				}
			} else {
				if (type instanceof SerializedObjectStructure) {
					so.setSerializedObject(key, get(key, (SerializedObjectStructure) type));
				} else {
					so.set(key, get(key));
				}
			}
		}
		return so;
	}

}
