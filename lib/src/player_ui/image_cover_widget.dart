part of '../music_player_page.dart';


class GetImageCover extends StatelessWidget {
  final Future<Uint8List?> futureResource;
  final String defaultValue;
  final BoxFit fit;
  final String? songId;
  final double? height;
  final double? width;

  const GetImageCover({
    required this.futureResource,
    this.defaultValue = "assets/no_cover4.png",
    this.fit = BoxFit.cover,
    this.songId,Key? key,
    this.height, this.width,
  }) : super(key: key);


  @override
  Widget build(BuildContext context) {

    //futureResource = futureResource ?? playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: songId);
    return FutureBuilder<Uint8List?>(
      //future: playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: actualMusic.id),
        future: futureResource,
        //initialData: ,
        builder: (_, snapshot) {
          if (snapshot.hasError || snapshot.data == null || snapshot.data!.lengthInBytes == 0) {
            return Image.asset(defaultValue,
              fit: fit,
              height: height ,
              width: width,
              gaplessPlayback: true,
            );
          }

          return Image.memory(snapshot.data!,
            fit: fit,
            height: height,
            width: width,
            gaplessPlayback: true,
          );

        }
    );
  }

}

class GetImageCoverBackground extends StatelessWidget {
  final Future<Uint8List?> futureResource;
  final String defaultValue;
  final BoxFit fit;
  final String? songId;
  final double? height;
  final double? width;

  const GetImageCoverBackground({
    required this.futureResource,
    this.defaultValue = "assets/no_cover4.png",
    this.fit = BoxFit.cover,
    this.songId,Key? key, this.height, this.width,
  }) : super(key: key);


  @override
  Widget build(BuildContext context) {

    //futureResource = futureResource ?? playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: songId);
    return FutureBuilder<Uint8List?>(
      //future: playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: actualMusic.id),
        future: futureResource,
        //initialData: ,
        builder: (_, snapshot) {
          if (snapshot.hasError || snapshot.data == null || snapshot.data!.lengthInBytes == 0) {
            return Container(
              height: height,
              width: width,
              decoration: BoxDecoration(
                image: DecorationImage(
                    image: AssetImage(defaultValue),
                  fit: fit,
                ),
              ),
            );
          }

          return Container(
            height: height,
            width: width,
            decoration: BoxDecoration(
              image: DecorationImage(
                  image: MemoryImage(snapshot.data!),
                fit: fit
              ),
            ),
          );

        }
    );
  }

}

class GetImageCoverItem extends StatelessWidget {
  final Future<Uint8List?> futureResource;
  final String defaultValue;
  final BoxFit fit;
  final String? songId;

  const GetImageCoverItem({
    required this.futureResource,
    this.defaultValue = "assets/no_cover4.png",
    this.fit = BoxFit.cover,
    this.songId,Key? key,
  }) : super(key: key);


  @override
  Widget build(BuildContext context) {

    //futureResource = futureResource ?? playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: songId);
    return FutureBuilder<Uint8List?>(
      //future: playerBloc.audioQuery.getArtwork(type: ResourceType.SONG, id: actualMusic.id),
        future: futureResource,
        //initialData: ,
        builder: (_, snapshot) {
          if (snapshot.hasError || snapshot.data == null || snapshot.data!.lengthInBytes == 0) {
            return Container(
              width: 55,
              height: 55,
              decoration: BoxDecoration(
                image: DecorationImage(
                  image: AssetImage(defaultValue),
                  fit: fit,
                ),
              ),
            );
          }
          return Image.memory(snapshot.data!, filterQuality: FilterQuality.high);
          /*return Container(
            height: 55,
            width: 55,
            decoration: BoxDecoration(
              image: DecorationImage(
                  image: MemoryImage(snapshot.data!,),
                  fit: fit
              ),
            ),
          );*/

        }
    );
  }

}