# @whiteguru/capacitor-plugin-media

Capacitor plugin to handle media files

## Breaking Changes

Since version 7.0.2 the following methods have been removed:

- `getMedias()`
- `getAlbums()`
- `createAlbum()`

## Install (Capacitor 7.x)

```bash
npm install @whiteguru/capacitor-plugin-media
npx cap sync
```

## Install (Capacitor 6.x)

```bash
npm install @whiteguru/capacitor-plugin-media@^6.0.2
npx cap sync
```

### or for Capacitor 5.x

```bash
npm install @whiteguru/capacitor-plugin-media@^5.0.2
npx cap sync
```

### or for Capacitor 4.x

```bash
npm install @whiteguru/capacitor-plugin-media@^4.1.1
npx cap sync
```

### or for Capacitor 3.x

```bash
npm install @whiteguru/capacitor-plugin-media@^3.0.1
npx cap sync
```

## Android

This API requires the following permissions be added to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="29"/>
```

Read about [Setting Permissions](https://capacitorjs.com/docs/android/configuration#setting-permissions) in the [Android Guide](https://capacitorjs.com/docs/android) for more information on setting Android permissions.

## API

<docgen-index>

- [`savePhoto(...)`](#savephoto)
- [`saveVideo(...)`](#savevideo)
- [`saveGif(...)`](#savegif)
- [`saveDocument(...)`](#savedocument)
- [`saveAudio(...)`](#saveaudio)
- [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### savePhoto(...)

```typescript
savePhoto(options?: MediaSaveOptions | undefined) => Promise<MediaResponse>
```

Add image to gallery. Creates album if not exists.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#mediasaveoptions">MediaSaveOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaresponse">MediaResponse</a>&gt;</code>

---

### saveVideo(...)

```typescript
saveVideo(options?: MediaSaveOptions | undefined) => Promise<MediaResponse>
```

Add video to gallery. Creates album if not exists.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#mediasaveoptions">MediaSaveOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaresponse">MediaResponse</a>&gt;</code>

---

### saveGif(...)

```typescript
saveGif(options?: MediaSaveOptions | undefined) => Promise<MediaResponse>
```

Add gif to gallery. Creates album if not exists.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#mediasaveoptions">MediaSaveOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaresponse">MediaResponse</a>&gt;</code>

---

### saveDocument(...)

```typescript
saveDocument(options?: MediaSaveOptions | undefined) => Promise<MediaResponse>
```

Add document to gallery. Android only. Create album if not exists.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#mediasaveoptions">MediaSaveOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaresponse">MediaResponse</a>&gt;</code>

---

### saveAudio(...)

```typescript
saveAudio(options?: MediaSaveOptions | undefined) => Promise<MediaResponse>
```

Add audio to gallery. Android only. Creates album if not exists.

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#mediasaveoptions">MediaSaveOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#mediaresponse">MediaResponse</a>&gt;</code>

---

### Interfaces

#### MediaResponse

| Prop       | Type                | Description |
| ---------- | ------------------- | ----------- |
| **`path`** | <code>string</code> | Media path  |
| **`name`** | <code>string</code> | Media name  |

#### MediaSaveOptions

| Prop        | Type                                         | Description                                                                         |
| ----------- | -------------------------------------------- | ----------------------------------------------------------------------------------- |
| **`path`**  | <code>string</code>                          | Path of file to add                                                                 |
| **`album`** | <code>{ id?: string; name?: string; }</code> | Album to add media. If no 'id' and 'name' not exists, album 'name' will be created. |

</docgen-api>
