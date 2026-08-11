import * as readline from 'node:readline';
import { stdin as input, stdout as output } from 'node:process';
import { ABRO } from './abro';

type Variables = {
  A: boolean;
  B: boolean;
  R: boolean;
  O: boolean;
  '#ticktime': number;
};

export async function cli(): Promise<void> {
  const { model, context } = ABRO(false);
  let ticktime = 0;

  const rl = readline.createInterface({
    input,
    output,
    terminal: false,
  });

  const sendVariables = (): void => {
    const json: Variables = {
      A: context.A,
      B: context.B,
      R: context.R,
      O: context.O,
      '#ticktime': ticktime,
    };
    console.log(JSON.stringify(json));
  };

  const receiveVariables = (line: string): void => {
    try {
      const json = JSON.parse(line) as Partial<Variables>;

      if (typeof json.A === 'boolean') context.A = json.A;
      if (typeof json.B === 'boolean') context.B = json.B;
      if (typeof json.R === 'boolean') context.R = json.R;
      if (typeof json.O === 'boolean') context.O = json.O;
      if (typeof json['#ticktime'] === 'number') ticktime = json['#ticktime'];
    } catch {
      return;
    }
  };

  model.reset();
  sendVariables();

  for await (const line of rl) {
    receiveVariables(line);

    const start = process.hrtime.bigint();
    model.tick();
    ticktime = Number(process.hrtime.bigint() - start);

    sendVariables();
  }
}

if (require.main === module) {
  void cli();
}