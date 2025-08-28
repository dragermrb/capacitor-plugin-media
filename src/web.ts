import { WebPlugin } from '@capacitor/core';

import type { MediaPlugin, MediaSaveOptions, MediaResponse } from './definitions';

export class MediaWeb extends WebPlugin implements MediaPlugin {
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
}
