part of '../music_player_page.dart';

class ControlButtons extends StatelessWidget {

  //final List<Song> _songInfo;

  const ControlButtons({Key? key})
      : super(key: key);

  //const ControlButtons(this.player, this.songInfo);

  void _setMediaNotification() {
    /*MediaNotification.hideNotification();
    MediaNotification.showNotification(
      title: songInfo[player.currentIndex].title,
      author: songInfo[player.currentIndex].artist,
      isPlaying: player.playing,
    );*/
  }

  @override
  Widget build(BuildContext context) {
    PlayerController musicController = BlocProvider.of<PlayerController>(context);
    //final songData = OtherBlocProvider.of<PlayerBloc>(context).songData;
    double iSizeNB = 30;

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        StreamBuilder<bool>(
          stream: musicController.player.shuffleModeEnabledStream,
          builder: (context, snapshot) {
            final shuffleModeEnabled = snapshot.data ?? false;
            return IconButton(
              icon: shuffleModeEnabled
                  ? Icon(CupertinoIcons.shuffle,
                      color: Theme.of(context).primaryColorLight)
                  : const Icon(CupertinoIcons.shuffle, color: Colors.grey),
              onPressed: () async {
                final enable = !shuffleModeEnabled;
                musicController.shuffle(enable);
                /*if (enable) {
                  await musicController.player.shuffle();
                }
                await musicController.player.setShuffleModeEnabled(enable);*/
              },
            );
          },
        ),
        /*IconButton(
          icon: Icon(Icons.volume_up),
          onPressed: () {
            _showSliderDialog(
              context: context,
              title: "Adjust volume",
              divisions: 10,
              min: 0.0,
              max: 1.0,
              stream: player.volumeStream,
              onChanged: player.setVolume,
            );
          },
        ),*/
        StreamBuilder<SequenceState?>(
          stream: musicController.player.sequenceStateStream,
          builder: (context, snapshot) => IconButton(
            //color: iconWhiteColor, //rewind,
            iconSize: iSizeNB,
            icon: const Icon(CupertinoIcons.backward_end_alt),
            onPressed: musicController.player.hasPrevious
                ? () {
                    musicController.prev();
                    _setMediaNotification();
                    //songData.setCurrentIndex(player.currentIndex);
                  }
                : null,
          ),
        ),
        Container(
          margin: const EdgeInsets.symmetric(horizontal: 16.0),
          child: StreamBuilder<PlayerState>(
            stream: musicController.player.playerStateStream,
            builder: (context, snapshot) {
              final playerState = snapshot.data;
              final processingState = playerState?.processingState;
              final playing = playerState?.playing;
              if (processingState == ProcessingState.loading ||
                  processingState == ProcessingState.buffering) {
                return Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: Theme.of(context).primaryColor,
                  ),
                  child: Container(
                    margin: const EdgeInsets.all(8.0),
                    width: 60.0,
                    height: 60.0,
                    child: const CircularProgressIndicator(
                      valueColor: AlwaysStoppedAnimation<Color>(Colors.grey),
                    ),
                  ),
                );
              } else if (playing != true) {
                return Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle, //const CircleBorder(),
                    color: Theme.of(context).primaryColor,
                  ),
                  child: IconButton(
                    icon: const Center(child: Icon(Icons.play_arrow_outlined)),
                    iconSize: 60,
                    onPressed: () {
                      musicController.play();
                      _setMediaNotification();
                    },
                  ),
                );
              } else if (processingState != ProcessingState.completed) {
                return Container(
                  decoration: BoxDecoration(
                      shape: BoxShape.circle,
                    color: Theme.of(context).primaryColor,
                  ),
                  child: IconButton(
                    icon: const Icon(CupertinoIcons.pause),
                    iconSize: 60.0,
                    onPressed: () {
                      musicController.pause();
                      _setMediaNotification();
                    },
                  ),
                );
              } else {
                return IconButton(
                  icon: const Icon(CupertinoIcons.memories),
                  iconSize: 64.0,
                  onPressed: () => musicController.setPosition(
                      Duration.zero,
                      index: musicController.player.effectiveIndices!.first),
                );
              }
            },
          ),
        ),
        StreamBuilder<SequenceState?>(
          stream: musicController.player.sequenceStateStream,
          builder: (context, snapshot) => IconButton(
            icon: const Icon(CupertinoIcons.forward_end_alt),
            iconSize: iSizeNB,
            onPressed: musicController.player.hasNext
                ? () {
                    musicController.next();
                    _setMediaNotification();
                    //songData.setCurrentIndex(player.currentIndex);
                  }
                : null,
          ),
        ),
        StreamBuilder<LoopMode>(
          stream: musicController.player.loopModeStream,
          builder: (context, snapshot) {
            final loopMode = snapshot.data ?? LoopMode.off;
            var icons = [
              const Icon(CupertinoIcons.repeat, color: Colors.grey),
              Icon(CupertinoIcons.repeat, color: Theme.of(context).primaryColor),
              Icon(CupertinoIcons.repeat_1, color: Theme.of(context).primaryColor),
            ];
            const cycleModes = [
              LoopMode.off,
              LoopMode.all,
              LoopMode.one,
            ];
            final index = cycleModes.indexOf(loopMode);
            return IconButton(
              icon: icons[index],
              onPressed: () {
                musicController.player.setLoopMode(cycleModes[
                    (cycleModes.indexOf(loopMode) + 1) % cycleModes.length]);
              },
            );
          },
        ),
        /*StreamBuilder<double>(
          stream: player.speedStream,
          builder: (context, snapshot) => IconButton(
            icon: Text("${snapshot.data?.toStringAsFixed(1)}x",
                style: TextStyle(fontWeight: FontWeight.bold)),
            onPressed: () {
              _showSliderDialog(
                context: context,
                title: "Adjust speed",
                divisions: 10,
                min: 0.5,
                max: 1.5,
                stream: player.speedStream,
                onChanged: player.setSpeed,
              );
            },
          ),
        ),*/
      ],
    );
  }
}

class SeekBar extends StatefulWidget {
  final Duration duration;
  final Duration position;
  final ValueChanged<Duration>? onChanged;
  final ValueChanged<Duration>? onChangeEnd;

  const SeekBar({
      required this.duration,
      required this.position,
      this.onChanged,
      this.onChangeEnd,
      Key? key}) : super(key: key);

  @override
  _SeekBarState createState() => _SeekBarState();
}

class _SeekBarState extends State<SeekBar> {
  double? _dragValue;

  /// Format [Duration] into a sting like => 3:22
  String fromDuration(Duration duration) {
    return (duration.toString().split('.').first).substring(2);
  }

  /// Timer Text with custom TextStyle
  Text textTimerStyle(String data, double scale) {
    return Text(
      data,
      textScaleFactor: scale,
      textAlign: TextAlign.center,
      style: const TextStyle(color: Colors.white54, fontSize: 15.0),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        Slider(
          activeColor: Theme.of(context).primaryColor,
          inactiveColor: Colors.grey[500],
          min: 0.0,
          max: widget.duration.inMilliseconds.toDouble(),
          value: min(_dragValue ?? widget.position.inMilliseconds.toDouble(),
              widget.duration.inMilliseconds.toDouble()),
          onChanged: (value) {
            setState(() {
              _dragValue = value;
            });
            if (widget.onChanged != null) {
              widget.onChanged!(Duration(milliseconds: value.round()));
            }
          },
          onChangeEnd: (value) {
            if (widget.onChangeEnd != null) {
              widget.onChangeEnd!(Duration(milliseconds: value.round()));
            }
            _dragValue = null;
          },
        ),
        /*Container(
          margin: EdgeInsets.only(left: 10.0, right: 10.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              textTimerStyle(fromDuration(widget.position), 0.8),
              textTimerStyle(fromDuration(widget.duration), 0.8),
            ],
          ),
        ),*/
        Positioned(
          right: 16.0,
          left: 16.0,
          bottom: 0.0,
          top: 35.0,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              textTimerStyle(fromDuration(widget.position), 0.8),
              textTimerStyle(fromDuration(widget.duration), 0.8),
            ],
          ),
        ),
        /*Positioned(
          top: 0.0,
          left: 0.0,
          child: Text(
              RegExp(r'((^0*[1-9]\d*:)?\d{2}:\d{2})\.\d+$')
                  .firstMatch("$_remaining")
                  ?.group(1) ??
                  '$_remaining',
              style: Theme.of(context).textTheme.caption),
        ),*/
      ],
    );
  }

//Duration get _remaining => widget.duration - widget.position;
}

class SliderDialog {
  final BuildContext context;
  final String title;
  final int divisions;
  final double min;
  final double max;
  final Stream<double> stream;
  final ValueChanged<double> onChanged;

  SliderDialog({
    required this.context,
    required this.title,
    required this.divisions,
    required this.min,
    required this.max,
    required this.stream,
    required this.onChanged,
    String? valueSuffix = '',
  }) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: Theme.of(context).backgroundColor.withOpacity(0.9),
        title: Text(title, textAlign: TextAlign.center),
        content: StreamBuilder<double>(
          stream: stream,
          builder: (context, snapshot) => SizedBox(
            height: 100.0,
            child: Column(
              children: [
                Text('${snapshot.data?.toStringAsFixed(1)}$valueSuffix',
                    style: const TextStyle(
                        fontFamily: 'Fixed',
                        fontWeight: FontWeight.bold,
                        fontSize: 24.0)),
                Slider(
                  divisions: divisions,
                  min: min,
                  max: max,
                  value: snapshot.data ?? 1.0,
                  onChanged: onChanged,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

void _showSliderDialog({
  required BuildContext context,
  required String title,
  required int divisions,
  required double min,
  required double max,
  required Stream<double> stream,
  required ValueChanged<double> onChanged,
  String? valueSuffix = '',
}) {
  showDialog(
    context: context,
    builder: (context) => AlertDialog(
      backgroundColor: Theme.of(context).backgroundColor.withOpacity(0.9),
      title: Text(title, textAlign: TextAlign.center),
      content: StreamBuilder<double>(
        stream: stream,
        builder: (context, snapshot) => SizedBox(
          height: 100.0,
          child: Column(
            children: [
              Text('${snapshot.data?.toStringAsFixed(1)}$valueSuffix',
                  style: const TextStyle(
                      fontFamily: 'Fixed',
                      fontWeight: FontWeight.bold,
                      fontSize: 24.0)),
              Slider(
                divisions: divisions,
                min: min,
                max: max,
                value: snapshot.data ?? 1.0,
                onChanged: onChanged,
              ),
            ],
          ),
        ),
      ),
    ),
  );
}
