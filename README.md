# backgroundservice

Na

## Install

```bash
npm install backgroundservice
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`start()`](#start)
* [`stop()`](#stop)
* [`checkLocationPermission()`](#checklocationpermission)
* [`requestLocationPermission()`](#requestlocationpermission)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### start()

```typescript
start() => Promise<void>
```

--------------------


### stop()

```typescript
stop() => Promise<void>
```

--------------------


### checkLocationPermission()

```typescript
checkLocationPermission() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### requestLocationPermission()

```typescript
requestLocationPermission() => Promise<{ value: string; }>
```

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

</docgen-api>
