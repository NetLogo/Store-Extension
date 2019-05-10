# Store

## What is it?

This extension provides primitives for writing and reading string values to and from a keyed, persistent data store.

## Why use it?

The primary purpose of this extension on NetLogo desktop is to provide compatibility with the NetLogo Web version.  This extension provides a unified way for a model to read and write data that persists across model loads in NetLogo desktop or NetLogo Web.

In NetLogo Web, due to the browser-based runtime environment, persistent storage available between model compiles or page refreshes must be provided in an asynchronous way using web APIs.  While NetLogo desktop can simply write and read to files to store data using the built-in file primitives, NetLogo Web has no way to interact with files on a user's computer without user interaction.

## Why to not use it?

If you need a way to store and retrieve key/value pairs inside a program, like a dictionary or map data structure, see the Table extension (desktop) or the nlmap extension (web).  If you need a way to write data to files for use outside of NetLogo, see the built-in `file-*` primitives (desktop) or the SendTo extension (web and desktop).  If you want to read data into a model from an external source, see the Fetch extension (web and desktop).

## Primitives

| Prim Name | Arguments                | Behavior
| --------- | ------------------------ | --------
| put       | *key* *value* *callback* | Stores the given string `value` with the given string `key`, overwriting any existing value for that key.  If the optional `callback` command block is given, it is called once the `put` is complete.
| get       | *key* *callback*         | Gets the string `value` for the given `key` and provides it as an argument to the given `callback` anonymous command.  If the given `key` is not in the store, it will raise an extension error.
| has-key   | *key* *callback*         | Checks if the given `key` exists in the store, and provides a true/false value as an argument to the given `callback` anonymous command.
| get-keys  | *callback*               | Gets a list of all keys in the store, and passes that list as an argument to the given `callback anonymous command.
| remove    | *key* *callback*         | Removes the given `key` and its associated value, if any, from the store.  If the optional `callback` command block is given, it is called once the `remove` is complete.
| clear     | *callback*               | Removes all keys and their values from the store.  If the optional `callback` command block is given, it is called once the `clear` is complete.

## Storage Location

The extension uses an H2 embedded database to store the key/value pairs.  The default storage location is in the user-specific application data folder, determined by operating system:

- On Windows, this is `%AppData%\NetLogo`
- On macOS, this is `~/Library/Application Support/NetLogo`
- On Linux, this is `~/.netlogo`

## Building

Open it in SBT.  If you successfully run `package`, `store.jar` is created.

## Terms of Use

[![CC0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

The NetLogo Store extension is in the public domain.  To the extent possible under law, Uri Wilensky has waived all copyright and related or neighboring rights.
