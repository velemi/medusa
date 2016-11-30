function handle(duringReplay) {
	object = e.getObject();

	if (objectMap.contains(object)) {
		instance.removeFromMap(object);
	}
}