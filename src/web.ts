import { WebPlugin } from '@capacitor/core';

import type {
  MediaAlbum,
  MediaAlbumCreate,
  MediaAlbumResponse,
  MediaFetchOptions,
  MediaPlugin,
  MediasResponse,
  MediaSaveOptions,
  MediaResponse,
} from './definitions';

export class MediaWeb extends WebPlugin implements MediaPlugin {
  getMedias(options?: MediaFetchOptions): Promise<MediasResponse> {
    console.log('getMedias', options);
    throw this.unimplemented('Not implemented on web.');
  }
  getAlbums(): Promise<MediaAlbumResponse> {
    throw this.unimplemented('Not implemented on web.');
  }
  savePhoto(options?: MediaSaveOptions): Promise<MediaResponse> {
    console.log('savePhoto', options);
    throw this.unimplemented('Not implemented on web.');
  }
  saveVideo(options?: MediaSaveOptions): Promise<MediaResponse> {
    console.log('saveVideo', options);
    throw this.unimplemented('Not implemented on web.');
  }
  saveGif(options?: MediaSaveOptions): Promise<MediaResponse> {
    console.log('saveGif', options);
    throw this.unimplemented('Not implemented on web.');
  }
  saveDocument(options?: MediaSaveOptions): Promise<MediaResponse> {
    console.log('saveDocument', options);
    throw this.unimplemented('Not implemented on web.');
  }
  saveAudio(options?: MediaSaveOptions): Promise<MediaResponse> {
    console.log('saveAudio', options);
    throw this.unimplemented('Not implemented on web.');
  }
  createAlbum(options: MediaAlbumCreate): Promise<MediaAlbum> {
    console.log('createAlbum', options);
    throw this.unimplemented('Not implemented on web.');
  }
}
