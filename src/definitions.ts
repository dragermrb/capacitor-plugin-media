export interface MediaPlugin {
  /**
   * Get list of user medias. IOS only
   */
  getMedias(options?: MediaFetchOptions): Promise<MediasResponse>;

  /**
   * Get list of user albums. IOS and Android Api 29+
   */
  getAlbums(): Promise<MediaAlbumResponse>;

  /**
   * Add image to gallery. Creates album if not exists.
   */
  savePhoto(options?: MediaSaveOptions): Promise<MediaResponse>;

  /**
   * Add video to gallery. Creates album if not exists.
   */
  saveVideo(options?: MediaSaveOptions): Promise<MediaResponse>;

  /**
   * Add gif to gallery. Creates album if not exists.
   */
  saveGif(options?: MediaSaveOptions): Promise<MediaResponse>;

  /**
   * Add document to gallery. Android only. Create album if not exists.
   */
  saveDocument(options?: MediaSaveOptions): Promise<MediaResponse>;

  /**
   * Add audio to gallery. Android only. Creates album if not exists.
   */
  saveAudio(options?: MediaSaveOptions): Promise<MediaResponse>;

  /**
   * Create album. IOS only
   */
  createAlbum(options: MediaAlbumCreate): Promise<MediaAlbum>;
}

export interface MediaSaveOptions {
  /**
   * Path of file to add
   */
  path: string;

  /**
   * Album to add media. If no 'id' and 'name' not exists, album 'name' will be created.
   */
  album?: {
    /**
     * Album identifier to add media. Required if 'name' not present.
     */
    id?: string;

    /**
     * Album name. Not used when 'id' is present.
     */
    name?: string;
  };
}

export interface MediaFetchOptions {
  /**
   * The number of photos to fetch, sorted by last created date descending
   */
  quantity?: number;

  /**
   * The width of thumbnail to return
   */
  thumbnailWidth?: number;

  /**
   * The height of thumbnail to return
   */
  thumbnailHeight?: number;

  /**
   * The quality of thumbnail to return as JPEG (0-100)
   */
  thumbnailQuality?: number;

  /**
   * Which types of assets to return (currently only supports "photos")
   */
  types?: string;

  /**
   * Which album identifier to query in (get identifier with getAlbums())
   */
  albumIdentifier?: string;
}

export interface MediasResponse {
  medias: MediaAsset[];
}

export interface MediaAsset {
  /**
   * Platform-specific identifier
   */
  identifier: string;

  /**
   * Data for a photo asset as a base64 encoded string (JPEG only supported)
   */
  data: string;

  /**
   * ISO date string for creation date of asset
   */
  creationDate: string;

  /**
   * Full width of original asset
   */
  fullWidth: number;

  /**
   * Full height of original asset
   */
  fullHeight: number;

  /**
   * Width of thumbnail preview
   */
  thumbnailWidth: number;

  /**
   * Height of thumbnail preview
   */
  thumbnailHeight: number;

  /**
   * Location metadata for the asset
   */
  location: MediaLocation;
}

export interface MediaLocation {
  /**
   * GPS latitude image was taken at
   */
  latitude: number;

  /**
   * GPS longitude image was taken at
   */
  longitude: number;

  /**
   * Heading of user at time image was taken
   */
  heading: number;

  /**
   * Altitude of user at time image was taken
   */
  altitude: number;

  /**
   * Speed of user at time image was taken
   */
  speed: number;
}

export interface MediaAlbumResponse {
  /**
   * Array of MediaAlbum
   */
  albums: MediaAlbum[];
}

export interface MediaAlbum {
  /**
   * Platform-specific album identifier
   */
  identifier?: string;

  /**
   * Album name
   */
  name: string;

  /**
   * Album type
   */
  type?: MediaAlbumType;
}

export declare enum MediaAlbumType {
  /**
   * Album is a "smart" album (such as Favorites or Recently Added)
   */
  Smart = 'smart',

  /**
   * Album is a cloud-shared album
   */
  Shared = 'shared',

  /**
   * Album is a user-created album
   */
  User = 'user',
}

export interface MediaAlbumCreate {
  /**
   * Album name
   */
  name: string;
}

export interface MediaResponse {
  /**
   * Media path
   */
  path: string;

  /**
   * Media name
   */
  name: string;
}
