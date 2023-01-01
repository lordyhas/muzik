
import 'package:flutter/material.dart';

import '../pages/app_settings/settings_pages.dart';

typedef OnSelected = void Function(int index);

class PlaylistDialogContent extends StatefulWidget {
  final List<TextThemeOption> options;
  final OnSelected? onSelected;
  final int initialIndexSelected;

  const PlaylistDialogContent({
    required this.options,
    required this.initialIndexSelected,
    this.onSelected,
    Key? key,
  }) : super(key: key);

  @override
  _PlaylistDialogContentState createState() => _PlaylistDialogContentState();
}

class _PlaylistDialogContentState extends State<PlaylistDialogContent> {
  late int selectedIndex;

  @override
  void initState() {
    super.initState();
    selectedIndex = widget.initialIndexSelected;
  }

  @override
  Widget build(BuildContext context) {
    return Scrollbar(
      child: ListView.builder(
          shrinkWrap: true,
          itemCount: widget.options.length,
          itemBuilder: (context, index) {
            return RadioListTile<int>(
              //selectedTileColor: Colors.white, //Theme.of(context).accentColor,
              activeColor: Theme.of(context).colorScheme.secondary,
                title: Text(widget.options[index].text),
                value: widget.options[index].index,
                groupValue: selectedIndex,
                onChanged: (value) {
                  setState(() {
                    selectedIndex = value!;
                  });
                  if (widget.onSelected != null) widget.onSelected!(selectedIndex);
                });
          }),
    );
  }
}

