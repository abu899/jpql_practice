package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {
    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            member.setMemberType(MemberType.ADMIN);
            em.persist(member);


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }

    public static void typedQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m ", Member.class);
        Query query1 = em.createQuery("select m.age, m.username from Member m ");

        List<Member> resultList = query.getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1.getUsername() = " + member1.getUsername());
        }
    }

    public static void parameterQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m where m.username = :username", Member.class);
        query.setParameter("username", "member1");
        List<Member> resultList = query.getResultList();
        for (Member member1 : resultList) {
            System.out.println("member1.getUsername() = " + member1.getUsername());
        }
    }

    public static void dtoQuery(EntityManager em) {
        TypedQuery<MemberDTO> querydto = em.createQuery("select new jpql.MemberDTO(m.username, m.age) from Member m ", MemberDTO.class);
        List<MemberDTO> resultList1 = querydto.getResultList();

        for (MemberDTO memberDTO : resultList1) {
            System.out.println("memberDTO.getUsername() = " + memberDTO.getUsername());
            System.out.println("memberDTO.getAge() = " + memberDTO.getAge());
        }
    }

    public static void pagingQuery(EntityManager em) {
        TypedQuery<Member> query = em.createQuery("select m from Member m ", Member.class)
                .setFirstResult(1)
                .setMaxResults(10);
        List<Member> resultList = query.getResultList();
        for (Member member : resultList) {
            System.out.println("member.getUsername() = " + member.getUsername());
        }
    }

    public static void joinQuery(EntityManager em) {
        Team team = new Team();
        team.setName("teamA");
        em.persist(team);

        Member member = new Member();
        member.setUsername("member2");
        member.setAge(20);
        member.setTeam(team);
        em.persist(member);

        em.flush();
        em.clear();

        String query = "select m from Member m inner join m.team t";
        List<Member> resultList = em.createQuery(query, Member.class).getResultList();

    }

    public static void enumWithQuery(EntityManager em) {
        String query = "select m from Member m where m.memberType = jpql.MemberType.ADMIN";
        List<Member> resultList = em.createQuery(query, Member.class).getResultList();

        String queryWithParameter = "select m from Member m where m.memberType = :userType";
        List<Member> resultList2 = em.createQuery(queryWithParameter, Member.class)
                .setParameter("userType", MemberType.ADMIN)
                .getResultList();
    }

    public static void caseQuery(EntityManager em) {
        String query =
                "select " +
                "case when m.age <= 10 then '학생요금' " +
                "   when m.age >= 60 then '경로요금' " +
                "   else '일반요금' end " +
                "from Member m";
        List<String> resultList = em.createQuery(query, String.class).getResultList();
    }

    public static void basicFunctionQuery(EntityManager em) {
        String query = "select concat('a', 'b') From Member m";
        String locateQuery = "select locate('de', 'abcdefgf') from Member m";

        List<String> resultList = em.createQuery(query, String.class).getResultList();
    }

    public static void userDefinedFunctionQuery(EntityManager em) {

        String query = "select function('group_concat', m.username) from Member m";

        List<String> resultList = em.createQuery(query, String.class).getResultList();
        for (String s : resultList) {
            System.out.println("s = " + s);
        }
    }

    public static void fetchJoinQuery(EntityManager em) {
        setTeamAndMember(em);

        // Lazy loading으로 인해 team이 필요한 횟수 만큼 쿼리가 추가로 나간다(N+1)
//        String query = "select m from Member m";
        String query = "select m from Member m join fetch m.team";
        List<Member> resultList = em.createQuery(query, Member.class).getResultList();

        for (Member resultMember : resultList) {
            System.out.println("resultMember.getUsername() = " + resultMember.getUsername()
                    + " resultMember.getTeamName = " + resultMember.getTeam().getName());
        }
    }

    // 컬렉션 패치 조인 (1대다)
    public static void fetchJoinWithDistinctQuery(EntityManager em) {
        setTeamAndMember(em);

        // 팀 입장에서 멤버가 N명이기에 데이터를 N개 만들어낸다
//        String query = "select t from Team t join fetch t.members";
        //language=JPAQL
        String query = "select distinct t from Team t join fetch t.members";
        List<Team> resultList = em.createQuery(query, Team.class).getResultList();

        for (Team team : resultList) {
            System.out.println("team.getName() = " + team.getName()
                    + " members = " + team.getMembers().size());
        }
    }

    public static void namedQuery(EntityManager em) {
        setTeamAndMember(em);

        List<Member> result = em.createNamedQuery("Member.findByUsername", Member.class)
                .setParameter("username", "회원1")
                .getResultList();
    }

    public static void bulkOperationQuery(EntityManager em) {
        setTeamAndMember(em);

        String query = "update Member m set m.age = 20";
        // 쿼리 날리면 자동으로 flush
        int resultCount = em.createQuery(query).executeUpdate();

        // 벌크 연산시 영속성 컨텍스트 초기화를 해주지 않으면 반영된 값을 가져올 수 없다
        Member findMember = em.find(Member.class, 1L);
        System.out.println("findMember.getAge() = " + findMember.getAge());

        em.clear(); // 영속성 컨텍스트 초기화

        Member findMember2 = em.find(Member.class, 1L);
        System.out.println("findMember2.getAge() = " + findMember2.getAge());

        System.out.println("resultCount = " + resultCount);
    }

    private static void setTeamAndMember(EntityManager em) {
        Team teamA = new Team();
        teamA.setName("teamA");
        em.persist(teamA);

        Team teamB = new Team();
        teamA.setName("teamB");
        em.persist(teamB);

        Member member = new Member();
        member.setUsername("회원1");
        member.setTeam(teamA);
        em.persist(member);

        Member member2 = new Member();
        member2.setUsername("회원2");
        member2.setTeam(teamA);
        em.persist(member2);

        Member member3 = new Member();
        member3.setUsername("회원3");
        member3.setTeam(teamB);
        em.persist(member3);
    }
}
