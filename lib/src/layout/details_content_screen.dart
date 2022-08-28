import 'dart:math' as math;
import 'dart:ui';

import 'dart:typed_data';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:get/get.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:muzik_audio_player/src/layout/music_layout.dart';
import 'package:muzik_audio_player/src/music_player_page.dart';
import 'package:muzik_audio_player/src/widget_model/mini_player.dart';
import 'package:muzik_audio_player/src/widget_model/no_data_widget.dart';
import 'package:on_audio_query/on_audio_query.dart';


part 'single_page/single_album_page.dart';
part 'single_page/single_artist_page.dart';
part 'single_page/quick_song_list_page.dart';




class QueueBottomSheetUI extends StatefulWidget {
  final Widget? child;
  final Color color;
  const QueueBottomSheetUI({required this.color, this.child, Key? key,}) : super(key: key);

  @override
  _QueueBottomSheetUIState createState() => _QueueBottomSheetUIState();
}

class _QueueBottomSheetUIState extends State<QueueBottomSheetUI>
    with SingleTickerProviderStateMixin {

  final double minHeight = 95;
  final double iconStartSize = 44;
  final double iconEndSize = 120;
  final double iconStartMarginTop = 36;
  final double iconEndMarginTop = 80;
  final double iconsVerticalSpacing = 24;
  final double iconsHorizontalSpacing = 16;

  late AnimationController _controller;

  double get maxHeight => MediaQuery.of(context).size.height;

  double get headerTopMargin =>
      lerp(20, 20 + MediaQuery.of(context).padding.top);

  double get headerFontSize => lerp(14, 24);

  double get itemBorderRadius => lerp(8, 24);

  double get iconLeftBorderRadius => itemBorderRadius;

  double get iconRightBorderRadius => lerp(8, 0);

  double get iconSize => lerp(iconStartSize, iconEndSize);

  double iconTopMargin(int index) =>
      lerp(iconStartMarginTop,
          iconEndMarginTop + index * (iconsVerticalSpacing + iconEndSize)) +
          headerTopMargin;

  double iconLeftMargin(int index) =>
      lerp(index * (iconsHorizontalSpacing + iconStartSize), 0);

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  double lerp(double min, double max) =>
      lerpDouble(min, max, _controller.value)!;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Positioned(
          height: lerp(minHeight, maxHeight),
          left: 0,
          right: 0,
          bottom: 0,
          child: GestureDetector(
            onTap: _toggle,
            onVerticalDragUpdate: _handleDragUpdate,
            onVerticalDragEnd: _handleDragEnd,
            child: Container(
              margin: const EdgeInsets.only(top: 35),
              padding: const EdgeInsets.symmetric(horizontal: 8.0).copyWith(top: 2.0),
              decoration:  BoxDecoration(
                color: widget.color,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
              ),
              child: Column(
                children: <Widget>[
                  SheetHeader(
                    fontSize: headerFontSize,
                    topMargin: headerTopMargin,
                  ),

                  Container(
                    child: widget.child,
                  ),
                  //for (Event event in events) _buildFullItem(event),
                  //for (Event event in events) _buildIcon(event),
                ],
              ),
            ),
          ),
        );
      },
    );
  }



  void _toggle() {
    final bool isOpen = _controller.status == AnimationStatus.completed;
    _controller.fling(velocity: isOpen ? -2 : 2);
  }

  void _handleDragUpdate(DragUpdateDetails details) {
    _controller.value -= (details.primaryDelta! / maxHeight);
  }

  void _handleDragEnd(DragEndDetails details) {
    if (_controller.isAnimating ||
        _controller.status == AnimationStatus.completed) return;

    final double flingVelocity =
        details.velocity.pixelsPerSecond.dy / maxHeight;
    if (flingVelocity < 0.0) {
      _controller.fling(velocity: math.max(2.0, -flingVelocity));
    } else if (flingVelocity > 0.0) {
      _controller.fling(velocity: math.min(-2.0, -flingVelocity));
    } else {
      _controller.fling(velocity: _controller.value < 0.5 ? -2.0 : 2.0);
    }
  }
}

class SheetHeader extends StatelessWidget {
  final double fontSize;
  final double topMargin;

  const SheetHeader({
    required this.fontSize,
    required this.topMargin,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
        height: 42,
        //top: topMargin,
        margin: const EdgeInsets.all(8.0),
        child: Row(
          children: [
            const SizedBox(width: 4.0,),
            /*IconButton(
              onPressed: (){},
              icon: const Icon(CupertinoIcons.chevron_down),
          ),*/
            const Spacer(),

            Column(
              children: [
                Container(
                  height: 2,
                  width: MediaQuery.of(context).size.width*0.10,
                  decoration: const BoxDecoration(
                      color: Colors.grey,
                      borderRadius: BorderRadius.all(Radius.circular(50))
                  ),
                ),
                const Spacer(),
                Text(
                  'Playlist ● En attente',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: fontSize,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const Spacer(),
              ],
            ),
            const Spacer(),
            /*IconButton(
            onPressed: (){},
            icon: const Icon(CupertinoIcons.double_music_note, color: Colors.transparent,),
          ),*/
            const SizedBox(width: 4.0,),

          ],
        )
    );
  }
}

