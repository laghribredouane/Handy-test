const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
const DATABASE_ID = "ai-studio-android-netfield-bd48c14d-1d7b-429f-97b3-ee1756fd23cf";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

let testEnv;

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: "127.0.0.1",
      port: 8085,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// Security Bounds Tests
test("Unauthenticated user: cannot read surveys", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("surveys").get());
});

test("Authenticated user: cannot read another user's survey", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("surveys").doc("alice_survey").set({
      userId: ALICE_UID,
      name: "Downtown 5G Drive",
      networkType: "5G",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("surveys").doc("alice_survey").get());
});

test("Authenticated user: can create valid survey for themselves", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("surveys").doc("survey_1").set({
      userId: ALICE_UID,
      name: "Highway LTE Benchmark",
      networkType: "LTE",
      operator: "Orange",
      avgRsrp: -85.5,
      avgSinr: 18.2,
      pci: 312,
      band: "Band 3 (1800 MHz)",
      pointCount: 150,
      status: "completed",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: fails creating survey for another user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("surveys").doc("survey_spoof").set({
      userId: BOB_UID,
      name: "Spoofed Survey",
      networkType: "5G",
      createdAt: new Date(),
    })
  );
});

test("Authenticated user: can query their own surveys", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("surveys").doc("alice_s1").set({
      userId: ALICE_UID,
      name: "Session 1",
      networkType: "5G",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("surveys").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: can create diagnostic result", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("diagnostics").doc("diag_1").set({
      userId: ALICE_UID,
      targetServer: "8.8.8.8",
      latencyMs: 24.5,
      downloadSpeedMbps: 88.2,
      jitterMs: 3.1,
      packetLossPercent: 0.0,
      rfVerdict: "EXCELLENT (5G Ultra-Capacity)",
      createdAt: new Date(),
    })
  );
});
