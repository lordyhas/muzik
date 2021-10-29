import 'package:flutter/material.dart';


abstract class CaseBuilderWidget extends StatelessWidget{
  const CaseBuilderWidget({Key? key,}) : super(key: key);
}

abstract class BooleanBuilderWidget extends StatelessWidget{
  const BooleanBuilderWidget({Key? key,}) : super(key: key);
}

class ContainerBooleanBuilder extends BooleanBuilderWidget {
  final bool check;
  final Widget
  ? ifTrue;
  final Widget
  ? ifFalse;

  const ContainerBooleanBuilder({
    this.check = true,
    this.ifTrue,
    this.ifFalse,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if(check) return Container(child: ifTrue,);
    return Container(child: ifFalse,);
  }
}

class BooleanBuilder extends BooleanBuilderWidget {
  final bool check;
  final Widget ifTrue;
  final Widget ifFalse;

  const BooleanBuilder({
    required this.check,
    required this.ifTrue,
    required this.ifFalse,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    if(check) return ifTrue;
    return ifFalse;
  }
}

class CaseBuilder extends CaseBuilderWidget{

  const CaseBuilder({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container();
  }

}
