import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';


class IconAsset extends StatelessWidget {
  final String path;
  final Color? color;
  final double? size;
  const IconAsset(this.path,{

    this.color,
    this.size,
    Key? key,})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    double height = MediaQuery.of(context).size.height;

    return Image.asset(path,
      width: size ?? Theme.of(context).iconTheme.size,
      height: size ?? Theme.of(context).iconTheme.size,
      color: color ?? Theme.of(context).iconTheme.color,);
  }
}
