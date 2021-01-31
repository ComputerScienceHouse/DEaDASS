import PasswordGenerator from "./password_generator";

function test_many(
  generator: PasswordGenerator,
  length: number,
  num_tests = 100
  // eslint-disable-next-line @typescript-eslint/type-annotation-spacing
): () => Promise<void[]> {
  return (): Promise<void[]> => {
    const promises = [];
    for (let index = 0; index < num_tests; index++) {
      promises.push(
        new Promise<void>((resolve) =>
          resolve(expect(generator.genPassword().length).toEqual(length))
        )
      );
    }
    return Promise.all(promises);
  };
}

describe("when no length is set the generator", () => {
  it(
    "generates 32 character passwords",
    test_many(new PasswordGenerator("./words.txt"), 32)
  );
});

describe("when the length is", () => {
  describe("16 the generator", () => {
    it(
      "generates 16 character passwords",
      test_many(new PasswordGenerator("./words.txt", 16), 16)
    );
  });
  describe("33 the generator", () => {
    it(
      "generates 33 character passwords",
      test_many(new PasswordGenerator("./words.txt", 33), 33)
    );
  });
  describe("64 the generator", () => {
    it(
      "generates 64 character passwords",
      test_many(new PasswordGenerator("./words.txt", 64), 64)
    );
  });
});
