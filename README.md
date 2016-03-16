ppmck
=====
A code to make your own NES Sound Files. This is my fork of ppmck9ex11-3

Compile
=======
On *nix (Mac OSX included)
```
cd src
sh make_binary.sh
```

the scripts include native + cross compile to windows. If you don't have the cross-compiler for windows,
the native build will still succeed ;) -- files output to parent "bin" directory

Test
====
On *nix:

```
cd songs
./mknsf.sh sample_auto_bank
# sample_auto_bank.nsf is created if all goes well
# I use Cog to play my NSF on Mac OSX
# For Windows, I hear nsfplay and virtuansf are recommended
