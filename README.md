# KiCoOO - Kieler Compiler for generating Object-Oriented languages

## Usage

To compile a SCChart using this compiler, you need to first convert it to a json representation using `kico` from the [`semantics`](https://github.com/kieler/semantics) repository. At the moment, the code is only available on the [`dam/json`](https://github.com/kieler/semantics/tree/dam/json) branch of the `semantics` repository. There might be a pre-compiled binary available in one of the github actions for said branch.

The most basic usage of `kico` is as follows:

```bash
java -jar kico.jar -s de.cau.cs.kieler.sccharts.SCTXToJSON -o <output_file_or_directory> <input_file>
```

This should generate a json representation of the SCChart in the specified output file or directory.

This json representation can then be used as input for the KiCoOO compiler. It has to be supplied on the standard input of the compiler. You also need to specify the output directory for the generated code as the first command line argument. The following command will generate code in the `output` directory:

```bash
java -jar kicooo.jar output < input.json
```

You can also specify a different target language for the generated code by passing it as the second command line argument. The default target language is `java`. The following command will generate code in the `output` directory for the `typescript` target language:

```bash
java -jar kicooo.jar output typescript < input.json
```

This will by default include a `Main.java` file with a sample interface for the generated code. You can disable this by passing the `--no-main` flag as the third command line argument to the compiler:

```bash
java -jar kicooo.jar output java --no-main < input.json
```

In addition to the generated code, you also need to include the runtime library for the target language in your project. This is currently not done automatically by the compiler. The runtime library for the `java` target language can be found in the [`misc`](./misc/)-folder of this repository. The runtime library for the `typescript` target language can currently only be found in the [`samples/abro-ts`](./samples/abro-ts/)-folder of this repository. You can copy the runtime library to your project and include it in your build process.

If you want to use referenced charts in your SCChart, you need to generate code for each of the referenced charts separately and include the generated code in your project. Remember to set the `--no-main` flag for all referenced charts, as you only want to have one `Main` class in your project.
