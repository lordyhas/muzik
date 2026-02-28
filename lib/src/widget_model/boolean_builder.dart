import 'package:flutter/material.dart';


abstract class CaseBuilderWidget extends StatelessWidget{
  const CaseBuilderWidget({super.key,});
}

abstract class BooleanBuilderWidget extends StatelessWidget{
  const BooleanBuilderWidget({super.key,});
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
    super.key,
  });

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
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    if(check) return ifTrue;
    return ifFalse;
  }
}

class CaseBuilder extends CaseBuilderWidget{

  const CaseBuilder({
    super.key,
  });

  @override
  Widget build(BuildContext context) {
    return Container();
  }

}
