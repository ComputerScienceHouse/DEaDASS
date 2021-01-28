import PasswordGenerator from "./password_generator";

let generator: PasswordGenerator;

describe("when no length is set the generator", () => {
  beforeEach(() => {
    generator = new PasswordGenerator("./words.txt");
  });

  it("generates 32 character passwords", () => {
    for (let index = 0; index < 100; index++) {
      expect(generator.genPassword().length).toBe(32);
    }
  });
});

describe("when the length is", () => {
  describe("16 the generator", () => {
    beforeEach(() => {
      generator = new PasswordGenerator("./words.txt", 16);
    });
    it("generates 16 character passwords", () => {
      for (let index = 0; index < 100; index++) {
        expect(generator.genPassword().length).toBe(16);
      }
    });
  });
  describe("33 the generator", () => {
    beforeEach(() => {
      generator = new PasswordGenerator("./words.txt", 33);
    });
    it("generates 33 character passwords", () => {
      for (let index = 0; index < 100; index++) {
        expect(generator.genPassword().length).toBe(33);
      }
    });
  });
  describe("64 the generator", () => {
    beforeEach(() => {
      generator = new PasswordGenerator("./words.txt", 64);
    });
    it("generates 64 character passwords", () => {
      for (let index = 0; index < 100; index++) {
        expect(generator.genPassword().length).toBe(64);
      }
    });
  });
});
