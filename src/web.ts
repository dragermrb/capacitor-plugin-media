import { WebPlugin } from '@capacitor/core';

import type { MediaPlugin } from './definitions';

export class MediaWeb extends WebPlugin implements MediaPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
