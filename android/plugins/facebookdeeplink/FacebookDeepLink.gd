extends Node

class_name FacebookDeepLink

var plugin: Object

func _ready():
	plugin = Engine.get_singleton("FacebookDeepLink")
	if plugin:
		print("FacebookDeepLink plugin loaded")
	else:
		print("FacebookDeepLink plugin not found")

func fetch_deferred_deep_link():
	if plugin:
		# Pass our instance ID so the plugin can call back to this object
		plugin.fetchDeferredDeepLink(get_instance_id())
	else:
		push_error("FacebookDeepLink plugin not available")

func on_deep_link_received(target: String):
	print("Deep link received: ", target)
	# Override this method in your script to handle the deep link
	# Example: get_tree().change_scene_to_file(target)

